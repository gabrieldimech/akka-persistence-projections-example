package com;

import akka.actor.typed.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.persistence.typed.PersistenceId;
import com.kafka.KafkaProducer;
import com.newsarticles.actors.NewsArticleEntity;
import com.newsarticles.actors.NewsArticleSupervisor;
import com.newsarticles.messages.NewsArticleMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

        final Cluster cluster = Cluster.get(system);
        cluster.join(cluster.selfAddress());

        return system;
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