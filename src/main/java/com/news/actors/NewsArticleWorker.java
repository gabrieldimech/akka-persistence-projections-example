package com.news.actors;

import java.util.List;
import java.util.Objects;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.kafka.KafkaProducer;
import com.news.entities.NewsArticle;
import com.news.messages.PublishEventIntent;
import com.news.services.NewsArticleService;

public class NewsArticleWorker extends AbstractBehavior<PublishEventIntent> {
    private final NewsArticleService newsArticleService;
    private final KafkaProducer kafkaProducer;
    private String topic;

    public NewsArticleWorker(ActorContext<PublishEventIntent> context, KafkaProducer kafkaProducer) {
        super(context);
        this.newsArticleService = new NewsArticleService();//todo find how to autowire
        this.kafkaProducer = kafkaProducer;
    }

    public static Behavior<PublishEventIntent> create(KafkaProducer kafkaProducer) {
        return Behaviors.setup(context -> new NewsArticleWorker(context, kafkaProducer));
    }

    @Override
    public Receive<PublishEventIntent> createReceive() {
        return newReceiveBuilder().onMessage(PublishEventIntent.class, this::onFetch).build();
    }

    private Behavior<PublishEventIntent> onFetch(PublishEventIntent command) {
        getContext().getLog().info("Fetching news items for {}!", command.getTopic());
        //todo its fine to block here right?
        List<NewsArticle> newsArticles = newsArticleService.getNewsItemsByTopic(command.getTopic()).toCompletableFuture().join();

        System.out.println("Fetched news items for : " + command.getTopic() + " " + newsArticles);

        //todo publish on kafka topic here
        for (NewsArticle newsArticle : newsArticles) {
            kafkaProducer.sendMessage(newsArticle, "news.articles");
        }

        //todo, do we need a replyto?
        //command.replyTo.tell(new NewsItemsFetchResponse(command.getTopic(), getContext().getSelf(), newsItems));
        return this;
    }

    public static final class NewsItemsFetchRequest {
        public final String topic;
        public final ActorRef<NewsItemsFetchResponse> replyTo;

        public NewsItemsFetchRequest(String topic, ActorRef<NewsItemsFetchResponse> replyTo) {
            this.topic = topic;
            this.replyTo = replyTo;
        }
    }

    public static final class NewsItemsFetchResponse {
        public final String topic;
        public final ActorRef<NewsItemsFetchRequest> from;
        public final List<NewsArticle> newsArticles;

        public NewsItemsFetchResponse(String whom, ActorRef<NewsItemsFetchRequest> from, List<NewsArticle> newsArticles) {
            this.topic = whom;
            this.from = from;
            this.newsArticles = newsArticles;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NewsItemsFetchResponse newsItemsFetchResponse = (NewsItemsFetchResponse) o;
            return Objects.equals(topic, newsItemsFetchResponse.topic) &&
                Objects.equals(from, newsItemsFetchResponse.from);
        }

        @Override
        public int hashCode() {
            return Objects.hash(topic, from);
        }

        @Override
        public String toString() {
            return "NewsItemsFetchResponse{" +
                "whom='" + topic + '\'' +
                ", from=" + from +
                '}';
        }

    }

}