package demo.newsarticles.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import demo.newsarticles.entities.NewsArticle;
import demo.newsarticles.projection.NewsArticleEvents;

public interface NewsArticleDao {

    Boolean saveNewsArticleEvent(NewsArticleEvents.NewsArticleAddedEvent newsArticleAddedEvent) throws Exception;
    List<NewsArticle> getNewsArticles();
    Optional<? extends NewsArticleEvents.Event> getNewsArticleEvent(UUID id);
}
