package demo.newsarticles.actors;

import akka.actor.typed.ActorSystem;
import demo.newsarticles.entities.NewsArticle;
import demo.newsarticles.messages.ConsumeNewsArticle;
import demo.newsarticles.messages.NewsArticleMessage;
import demo.newsarticles.services.NewsArticleService;
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
