package com.news.messages;

import com.news.entities.NewsArticle;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsumeNewsArticle implements NewsArticleMessage {
    private NewsArticle newsArticle;
}
