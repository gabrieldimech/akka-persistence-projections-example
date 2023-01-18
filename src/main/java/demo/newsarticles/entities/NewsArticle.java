package demo.newsarticles.entities;

import java.util.Date;

import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class NewsArticle implements Jsonable {
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
