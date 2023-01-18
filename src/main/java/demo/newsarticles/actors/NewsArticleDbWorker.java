package demo.newsarticles.actors;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import demo.newsarticles.dao.NewsArticleDao;
import demo.newsarticles.projection.NewsArticleEvents;

public class NewsArticleDbWorker extends AbstractBehavior<NewsArticleEvents.NewsArticleAddedEvent> {
    private final NewsArticleDao newsArticleDao;

    public NewsArticleDbWorker(ActorContext<NewsArticleEvents.NewsArticleAddedEvent> context, NewsArticleDao newsArticleDao) {
        super(context);
        this.newsArticleDao = newsArticleDao;
    }

    @Override
    public Receive<NewsArticleEvents.NewsArticleAddedEvent> createReceive() {
        return newReceiveBuilder().build();
    }
}
