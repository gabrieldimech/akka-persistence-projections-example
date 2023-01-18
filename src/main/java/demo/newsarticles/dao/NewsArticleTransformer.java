package demo.newsarticles.dao;

import demo.newsarticles.entities.NewsArticle;
import demo.newsarticles.projection.NewsArticleEvents;

public class NewsArticleTransformer {

    public static NewsArticle fromEvent(NewsArticleEvents.NewsArticleAddedEvent newsArticleAddedEvent) {
        return NewsArticle.builder().id(newsArticleAddedEvent.getId())
            .description(newsArticleAddedEvent.getDescription())
            .link(newsArticleAddedEvent.getLink())
            .pubDate(newsArticleAddedEvent.getPubDate())
            .title(newsArticleAddedEvent.getTitle())
            .topic(newsArticleAddedEvent.getTopic())
            .build();
    }
}
