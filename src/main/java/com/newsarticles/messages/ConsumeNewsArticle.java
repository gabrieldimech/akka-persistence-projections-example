package com.newsarticles.messages;

import com.newsarticles.entities.NewsArticle;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsumeNewsArticle implements NewsArticleMessage {
    private NewsArticle newsArticle;
}
