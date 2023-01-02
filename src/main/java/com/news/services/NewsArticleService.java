package com.news.services;

import akka.actor.typed.ActorSystem;
import com.news.entities.NewsArticle;
import com.news.messages.ConsumeNewsArticle;
import com.news.messages.NewsArticleMessage;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Service
public class NewsArticleService {

    public CompletionStage<List<NewsArticle>> getNewsItemsByTopic(String topic) {
        return fetchFeedDocuments(topic)
            .thenCompose(syndFeed -> convertFeedIntoNewsItems(syndFeed, topic));
    }

    private CompletionStage<SyndFeed> fetchFeedDocuments(String topic) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL feedSource = new URL(constructUrl(topic));
                SyndFeedInput input = new SyndFeedInput();

                return input.build(new XmlReader(feedSource));
            } catch (Exception e) {
                throw new IllegalArgumentException("Error fetching documents for topic: " + topic, e);
            }
        });
    }

    private CompletionStage<List<NewsArticle>> convertFeedIntoNewsItems(SyndFeed syndFeed, String topic) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<NewsArticle> res = new ArrayList<>();

                List<SyndEntryImpl> entries = (List<com.sun.syndication.feed.synd.SyndEntryImpl>) syndFeed.getEntries();

                for (com.sun.syndication.feed.synd.SyndEntryImpl entry : entries) {
                    res.add(new NewsArticle(UUID.randomUUID().toString(), entry.getTitle(), entry.getLink(), topic, topic, entry.getPublishedDate()));
                }

                return res;
            } catch (Exception e) {
                throw new IllegalArgumentException("Error transforming feed to news items: " + topic, e);
            }
        });
    }

    private String constructUrl(String topic) {
        return "https://news.google.com/rss/search?q=" + topic + "+when:1h&ceid=US:en&hl=en-US&gl=US";
    }
}
