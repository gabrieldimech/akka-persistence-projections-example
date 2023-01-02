package com.newsarticles.actors;

import akka.actor.typed.ActorSystem;
import com.newsarticles.entities.NewsArticle;
import com.newsarticles.messages.ConsumeNewsArticle;
import com.newsarticles.messages.NewsArticleMessage;
import com.newsarticles.services.NewsArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(
    topics = "news.articles",
    containerFactory = "newsArticlesKafkaListenerContainerFactory")
public class NewsArticleListener {

    @Autowired
    NewsArticleService newsArticleService;
    @Autowired final ActorSystem<NewsArticleMessage> supervisor;

    public NewsArticleListener(ActorSystem<NewsArticleMessage> supervisor) { this.supervisor = supervisor; }

    @KafkaHandler
    public void newsArticleListener(NewsArticle newsArticle) {
        System.out.println("Consumed news article: " + newsArticle.toString());
        supervisor.tell(ConsumeNewsArticle.builder().newsArticle(newsArticle).build());
    }
}
