package com.opencredo.concourse.domain.events.batching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.time.TimeUUID;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class SubBatchingEventBatch implements EventBatch {

    public static EventBatch writingTo(Consumer<Collection<Event>> eventsConsumer, int maxSubBatchSize) {
        return new SubBatchingEventBatch(eventsConsumer, maxSubBatchSize);
    }

    private final UUID id = TimeUUID.timeBased();
    private final List<Event> events = new LinkedList<>();
    private final Consumer<Collection<Event>> eventsConsumer;
    private final int maxSubBatchSize;

    private SubBatchingEventBatch(Consumer<Collection<Event>> eventsConsumer, int maxSubBatchSize) {
        this.eventsConsumer = eventsConsumer;
        this.maxSubBatchSize = maxSubBatchSize;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void complete() {
        eventsConsumer.accept(events);
    }

    @Override
    public void accept(Event event) {
        events.add(event);
        if (events.size() == maxSubBatchSize) {
            eventsConsumer.accept(events);
            events.clear();
        }
    }
}
