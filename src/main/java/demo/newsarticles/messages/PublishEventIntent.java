package demo.newsarticles.messages;

import lombok.Value;

@Value
public class PublishEventIntent implements NewsArticleMessage {
    String topic;
}
