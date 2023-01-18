package demo.newsarticles.messages;

import com.lightbend.lagom.serialization.Jsonable;
import demo.newsarticles.entities.NewsArticle;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsumeNewsArticle implements NewsArticleMessage, Jsonable {
    private NewsArticle newsArticle;
}
