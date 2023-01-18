package demo.newsarticles.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import demo.newsarticles.entities.NewsArticle;
import demo.newsarticles.projection.NewsArticleEvents;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;

@AllArgsConstructor
public class NewsArticleDaoImpl implements NewsArticleDao {
    @Autowired
    private final Jdbi jdbi;

    @Override
    public Boolean saveNewsArticleEvent(NewsArticleEvents.NewsArticleAddedEvent newsArticleAddedEvent) throws Exception {
        return jdbi.inTransaction(handle -> {
            if (handle.createUpdate(
                    "INSERT INTO news_articles " +
                        "(`id`, `title`, `link`, `description`, `topic`, `pub_date`) VALUES " +
                        "(:id, :title, :link, :description, :topic, :pub_date)"
                )
                .bind("id", newsArticleAddedEvent.getId())
                .bind("title", newsArticleAddedEvent.getTitle())
                .bind("link", newsArticleAddedEvent.getLink())
                .bind("description", newsArticleAddedEvent.getDescription())
                .bind("topic", newsArticleAddedEvent.getTopic())
                .bind("pub_date", newsArticleAddedEvent.getPubDate())
                .execute() == 0) {
                throw new Exception("Failed to persist news article");
            }
            return true;
        });
    }

    @Override
    public List<NewsArticle> getNewsArticles() {
        return null;
    }

    @Override
    public Optional<? extends NewsArticleEvents.Event> getNewsArticleEvent(UUID id) {
        return Optional.empty();
    }
}
