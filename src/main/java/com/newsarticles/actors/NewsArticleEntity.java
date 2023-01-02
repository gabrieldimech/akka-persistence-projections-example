package com.newsarticles.actors;

import java.util.ArrayList;
import java.util.List;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.lightbend.lagom.serialization.Jsonable;
import com.newsarticles.entities.NewsArticle;
import org.slf4j.Logger;

public class NewsArticleEntity extends EventSourcedBehavior<
    NewsArticleEntity.Command, NewsArticleEntity.Event, NewsArticleEntity.State> {
    private static final String TAG_PREFIX = "NewsArticleEntity-";
    private final Logger log;
    /** Entity tag */
    private final String tag;
    public static final EntityTypeKey<Command> ENTITY_KEY = EntityTypeKey.create(Command.class, "NewsArticle");

    public interface Command { }

    public static class Add implements Command {
        public final NewsArticle data;

        public Add(NewsArticle data) {
            this.data = data;
        }
    }

    public enum Clear implements Command {
        INSTANCE
    }

    interface Event extends Jsonable { }

    public static class Added implements Event {
        public final NewsArticle data;

        public Added(NewsArticle data) {
            this.data = data;
        }
    }

    public enum Cleared implements Event {
        INSTANCE
    }

    public static class State {
        private final List<NewsArticle> items;

        private State(List<NewsArticle> items) {
            this.items = items;
        }

        public State() {
            this.items = new ArrayList<>();
        }

        public State addItem(NewsArticle data) {
            List<NewsArticle> newItems = new ArrayList<>(items);
            newItems.add(0, data);
            // keep 5 items
            List<NewsArticle> latest = newItems.subList(0, Math.min(5, newItems.size()));
            return new State(latest);
        }
    }

    public static Behavior<Command> create(PersistenceId persistenceId, int index) {
        return Behaviors.setup(context -> new NewsArticleEntity(persistenceId, context, constructTag(index)));
    }

    public static String constructTag(int index) {
        return TAG_PREFIX + index;
    }

    public NewsArticleEntity(PersistenceId persistenceId, ActorContext<Command> context, String tag) {
        super(persistenceId);
        this.log = context.getLog();
        this.tag = tag;
    }

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<Command, Event, State> commandHandler() {
        return newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(Add.class, command -> Effect().persist(new Added(command.data)))
            .onCommand(Clear.class, command -> Effect().persist(Cleared.INSTANCE))
            .build();
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
        return newEventHandlerBuilder()
            .forAnyState()
            .onEvent(Added.class, (state, event) -> state.addItem(event.data))
            .onEvent(Cleared.class, () -> new State())
            .build();
    }

}
