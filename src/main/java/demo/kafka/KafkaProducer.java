package demo.kafka;

import demo.newsarticles.entities.NewsArticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, NewsArticle> kafkaTemplate;

    public void sendMessage(NewsArticle newsArticle, String topic) {
        kafkaTemplate.send(topic, newsArticle.getTopic(), newsArticle);
    }
}
