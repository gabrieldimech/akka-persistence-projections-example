package demo.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsArticlePublishedEvent implements Event {
    private Long   timestamp;
    private String newsArticleTopic;
    public static final String TYPE = "NEWS_ARTICLE_PUBLISHED";

    @Override
    public String getType() {
        return TYPE;
    }

}
