package com.kafka;

import java.util.HashMap;
import java.util.Map;

import com.news.entities.NewsArticle;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${news.articles.groupId}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, NewsArticle> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            bootstrapAddress);
        props.put(
            ConsumerConfig.GROUP_ID_CONFIG,
            groupId);
        return new DefaultKafkaConsumerFactory<>(
            props,
            new StringDeserializer(),
            new JsonDeserializer<>(NewsArticle.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NewsArticle>
    newsArticlesKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, NewsArticle> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

}
