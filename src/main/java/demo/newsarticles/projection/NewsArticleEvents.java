package demo.newsarticles.projection;

import java.util.Date;

import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class NewsArticleEvents {

    public interface Event extends Jsonable {//todo avoided cbor due to config issues

        String getId();
    }

    @Getter
    @AllArgsConstructor
    public static final class NewsArticleAddedEvent implements Event {
        private String id, title, link, description, topic;
        private Date pubDate;
    }

}
