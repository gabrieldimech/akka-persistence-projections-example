package com.kafka;

import com.news.entities.NewsArticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, NewsArticle> kafkaTemplate;

    //todo
    //key used to determine partition
    //If there is no key provided, then Kafka will partition the data in a round-robin fashion.
    public void sendMessage(NewsArticle newsArticle, String topic) {
        kafkaTemplate.send(topic, newsArticle.getTopic(), newsArticle);
    }
}
