package com.news.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NewsArticle {
    private String id, title, link, description, topic;
    private Date pubDate;

    public String toString() {
        return new StringBuilder()
            .append(topic)
            .append(" - ")
            .append(title)
            .append(" - ")
            .append(description)
            .toString();
    }

}
