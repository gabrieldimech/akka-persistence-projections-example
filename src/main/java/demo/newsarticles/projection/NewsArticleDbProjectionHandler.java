package demo.newsarticles.projection;

import java.util.concurrent.CompletionStage;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.jdbc.JdbcSession;
import akka.projection.jdbc.javadsl.JdbcHandler;
import demo.newsarticles.actors.NewsArticleEntity;
import demo.newsarticles.dao.NewsArticleDao;

public class NewsArticleDbProjectionHandler extends JdbcHandler<EventEnvelope<NewsArticleEntity.Added>, JdbcSession> {
    private NewsArticleDao newsArticleDao;
    private ActorSystem<?> system;

    @Override
    public void process(JdbcSession session, EventEnvelope<NewsArticleEntity.Added> envelope) throws Exception {
        NewsArticleEntity.Added event = envelope.event();

        CompletionStage<Done> dbEffect = null;
        if (event instanceof NewsArticleEntity.Added) {
        }
        //return CompletableFuture.completedFuture(Done.getInstance());
    }
}
