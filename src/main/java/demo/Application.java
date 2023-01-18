package demo;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.sql.DataSource;

import akka.actor.typed.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.sharding.typed.ShardedDaemonProcessSettings;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.ShardedDaemonProcess;
import akka.persistence.jdbc.query.javadsl.JdbcReadJournal;
import akka.persistence.query.Offset;
import akka.persistence.query.PersistenceQuery;
import akka.persistence.typed.PersistenceId;
import akka.projection.ProjectionBehavior;
import akka.projection.ProjectionId;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.eventsourced.javadsl.EventSourcedProvider;
import akka.projection.javadsl.AtLeastOnceProjection;
import akka.projection.javadsl.SourceProvider;
import akka.projection.jdbc.JdbcSession;
import akka.projection.jdbc.javadsl.JdbcProjection;
import demo.kafka.KafkaProducer;
import demo.newsarticles.actors.NewsArticleEntity;
import demo.newsarticles.actors.NewsArticleSupervisor;
import demo.newsarticles.dao.NewsArticleDao;
import demo.newsarticles.dao.NewsArticleDaoImpl;
import demo.newsarticles.messages.NewsArticleMessage;
import demo.newsarticles.projection.NewsArticleDbProjectionHandler;
import demo.newsarticles.projection.ProjectionJdbcSession;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Configuration
public class Application {

    @Autowired
    private KafkaProducer kafkaProducer;
    ClusterSharding clusterSharding;

    @Bean
    public ActorSystem<NewsArticleMessage> actorSystem() {

        ActorSystem<NewsArticleMessage> system = ActorSystem.create(NewsArticleSupervisor.create(kafkaProducer), "newsArticleSupervisor");

        clusterSharding = clusterSharding(system);

        initNewsArticleEntity(clusterSharding, 10);

        initProjections(system, jdbiAkka());

        final Cluster cluster = Cluster.get(system);
        cluster.join(cluster.selfAddress());

        return system;
    }

    @Bean
    public NewsArticleDaoImpl initDao(Jdbi jdbi) {
        return new NewsArticleDaoImpl(jdbi);
    }

    @Bean
    public static ClusterSharding clusterSharding(ActorSystem actorSystem) {
        return ClusterSharding.get(actorSystem);
    }

    private void initNewsArticleEntity(ClusterSharding clusterSharding, int maxTags) {
        clusterSharding.init(
            Entity.of(NewsArticleEntity.ENTITY_KEY,
                entityContext -> {
                    int i = Math.abs(entityContext.getEntityId().hashCode() % maxTags);
                    return NewsArticleEntity.create(
                        PersistenceId.of(NewsArticleEntity.ENTITY_KEY.name(), entityContext.getEntityId()),
                        i);
                }));
    }

    private void initProjections(ActorSystem system, Jdbi jdbi) {
        Function<String, Supplier<JdbcSession>> projectionFunc = (name) -> () -> new ProjectionJdbcSession(jdbi);

        NewsArticleDao newsArticleDao = initDao(jdbi);

        ShardedDaemonProcess.get(system)
            .init(
                ProjectionBehavior.Command.class,
                "NewsArticleDbProjectionHandler",
                10,
                index -> ProjectionBehavior.create(createProjectionFor(system, index, projectionFunc)),
                ShardedDaemonProcessSettings.create(system),
                Optional.of(ProjectionBehavior.stopMessage()));
    }

    private static <T extends NewsArticleEntity.Added> AtLeastOnceProjection<Offset, EventEnvelope<NewsArticleEntity.Added>> createProjectionFor(
        akka.actor.typed.ActorSystem<?> system, int index, Function<String, Supplier<JdbcSession>> jdbcSessionCreator) {

        var tag = NewsArticleEntity.constructTag(index);

        SourceProvider<Offset, EventEnvelope<NewsArticleEntity.Added>> sourceProvider =
            EventSourcedProvider.eventsByTag(system, JdbcReadJournal.Identifier(), tag);

        System.out.println("CREATED projection for tag : " + tag);

        int saveOffsetAfterEnvelopes = 100;
        Duration saveOffsetAfterDuration = Duration.ofMillis(500);

        return JdbcProjection.atLeastOnce(
                ProjectionId.of("NewsArticleDbProjectionHandler", tag),
                sourceProvider,
                jdbcSessionCreator.apply("NewsArticleDbProjectionHandler"),
                NewsArticleDbProjectionHandler::new,
                system)
            .withSaveOffset(saveOffsetAfterEnvelopes, saveOffsetAfterDuration)
            .withRestartBackoff(
                Duration.ofSeconds(5), /*minBackoff*/
                Duration.ofSeconds(30), /*maxBackoff*/
                0.5 /*randomFactor*/);
    }

    @Bean
    public JdbcReadJournal providesJdbcReadJournal(final ActorSystem actorSystem) {
        return PersistenceQuery.get(actorSystem).getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource driverManagerDataSource() {
        return new DriverManagerDataSource();
    }

    @Bean
    @ConfigurationProperties(prefix = "akka-persistence-jdbc.shared-databases.slick.db")
    public DataSource driverManagerDataSourceAkka() {
        return new DriverManagerDataSource();
    }

    @Bean
    public Jdbi jdbi() {
        return Jdbi.create(driverManagerDataSource())
            .installPlugin(new SqlObjectPlugin());
    }

    @Bean
    public Jdbi jdbiAkka() {
        return Jdbi.create(driverManagerDataSourceAkka())
            .installPlugin(new SqlObjectPlugin());
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}