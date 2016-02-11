package com.opencredo.concourse.domain.events;

import com.opencredo.concourse.domain.events.batching.EventBatch;

import java.util.function.Consumer;

@FunctionalInterface
public interface EventBus extends Consumer<Event> {

    EventBatch startBatch();

    @Override
    default void accept(Event event) {
        dispatch(batch -> batch.accept(event));
    }

    default void dispatch(Consumer<Consumer<Event>> batchConsumer) {
        EventBatch batch = startBatch();
        batchConsumer.accept(batch);
        batch.complete();
    }
}
