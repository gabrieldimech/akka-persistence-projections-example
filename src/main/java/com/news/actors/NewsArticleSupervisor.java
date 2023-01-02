package com.news.actors;

import java.util.Optional;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import com.Application;
import com.kafka.KafkaProducer;
import com.news.messages.ConsumeNewsArticle;
import com.news.messages.NewsArticleMessage;
import com.news.messages.PublishEventIntent;

public class NewsArticleSupervisor extends AbstractBehavior<NewsArticleMessage> {

    private ClusterSharding sharding;
    private KafkaProducer kafkaProducer;

    public NewsArticleSupervisor(ActorContext<NewsArticleMessage> context, KafkaProducer kafkaProducer) {
        super(context);
        this.sharding = Application.clusterSharding(context.getSystem());
        this.kafkaProducer = kafkaProducer;
    }

    public static Behavior<NewsArticleMessage> create(KafkaProducer kafkaProducer) {
        return Behaviors.setup(context -> new NewsArticleSupervisor(context, kafkaProducer));
    }

    @Override
    public Receive<NewsArticleMessage> createReceive() {
        return newReceiveBuilder().onMessage(PublishEventIntent.class, this::processPublishNewsItemMessage)
            .onMessage(ConsumeNewsArticle.class, this::processConsumeNewsItemMessage)
            .build();
    }

    private Behavior<NewsArticleMessage> processConsumeNewsItemMessage(ConsumeNewsArticle message) {
        EntityRef<NewsArticleEntity.Command> entityRef =
            sharding.entityRefFor(NewsArticleEntity.ENTITY_KEY, message.getNewsArticle().getId());//todo check if we need to inject sharding instead

        entityRef.tell(new NewsArticleEntity.Add(message.getNewsArticle()));

        return Behaviors.same();
    }

    //publishes news articles to kafka
    private Behavior<NewsArticleMessage> processPublishNewsItemMessage(PublishEventIntent message) {
        final Optional<ActorRef<Void>> childActorRef = getContext().getChild(buildWorkerName(message));
        if (childActorRef.isPresent()) {
            childActorRef.get().unsafeUpcast().tell(message);//todo ask about this
        } else {
            final ActorRef<PublishEventIntent> childRef = getContext()
                .spawn(Behaviors.setup(ctx -> NewsArticleWorker.create(kafkaProducer)),
                    buildWorkerName(message), DispatcherSelector.defaultDispatcher());

            childRef.unsafeUpcast().tell(message);//todo ask about this
        }
        return Behaviors.same();
    }

    private String buildWorkerName(PublishEventIntent message) {
        return NewsArticleWorker.class.getSimpleName() + "-" + message.getTopic();
    }
}
