package com;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.SpawnProtocol;
import akka.actor.typed.internal.adapter.GuardianStartupBehavior;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.Cluster;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.persistence.typed.PersistenceId;
import com.kafka.KafkaProducer;
import com.news.actors.NewsArticleEntity;
import com.news.actors.NewsArticleSupervisor;
import com.news.messages.NewsArticleMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static com.akka.integration.SpringExtension.SPRING_EXTENSION_PROVIDER;

@SpringBootApplication
@EnableScheduling
@Configuration
public class Application {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private KafkaProducer kafkaProducer;
    ClusterSharding clusterSharding;

    @Bean
    public ActorSystem<NewsArticleMessage> actorSystem() {

        ActorSystem<NewsArticleMessage> system = ActorSystem.create(NewsArticleSupervisor.create(kafkaProducer), "newsArticleSupervisor");

        SPRING_EXTENSION_PROVIDER.get(system)
            .initialize(applicationContext);

        clusterSharding = clusterSharding(system);

        initNewsArticleEntity(clusterSharding, 10);

        final Cluster cluster = Cluster.get(system);
        cluster.join(cluster.selfAddress());

        return system;

       /* return Adapter.spawn(
            actorSystem,
            restartOnFailure(Behaviors.setup(ctx -> create(injector, OfferingServiceSupervisor.class, ctx))),
            generateActorName(OfferingServiceSupervisor.class));*/
    }

    @Bean
    public static ClusterSharding clusterSharding(ActorSystem actorSystem) {
        return ClusterSharding.get(actorSystem);
    }

    private void initNewsArticleEntity(ClusterSharding clusterSharding, int maxTags) {
        clusterSharding.init(
            Entity.of(NewsArticleEntity.ENTITY_KEY,
                entityContext -> {
                    int i = Math.abs(entityContext.getEntityId().hashCode() % maxTags);//todo find out how to calculate tag amount
                    return NewsArticleEntity.create(
                        PersistenceId.of(entityContext.getEntityTypeKey().name(), entityContext.getEntityId()),
                        i);
                }));
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}