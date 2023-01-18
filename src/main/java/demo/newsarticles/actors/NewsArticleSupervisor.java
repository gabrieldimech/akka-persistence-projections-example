package demo.newsarticles.actors;

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
import demo.Application;
import demo.kafka.KafkaProducer;
import demo.newsarticles.messages.ConsumeNewsArticle;
import demo.newsarticles.messages.NewsArticleMessage;
import demo.newsarticles.messages.PublishEventIntent;

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
            sharding.entityRefFor(NewsArticleEntity.ENTITY_KEY, message.getNewsArticle().getId());

        entityRef.tell(new NewsArticleEntity.Add(message.getNewsArticle()));

        return Behaviors.same();
    }

    //publishes news articles to kafka
    private Behavior<NewsArticleMessage> processPublishNewsItemMessage(PublishEventIntent message) {
        final Optional<ActorRef<Void>> childActorRef = getContext().getChild(buildWorkerName(message));
        if (childActorRef.isPresent()) {
            childActorRef.get().unsafeUpcast().tell(message);
        } else {
            final ActorRef<PublishEventIntent> childRef = getContext()
                .spawn(Behaviors.setup(ctx -> NewsArticleWorker.create(kafkaProducer)),
                    buildWorkerName(message), DispatcherSelector.defaultDispatcher());

            childRef.unsafeUpcast().tell(message);
        }
        return Behaviors.same();
    }

    private String buildWorkerName(PublishEventIntent message) {
        return NewsArticleWorker.class.getSimpleName() + "-" + message.getTopic();
    }
}
