package com.opencredo.concourse.domain.events.batching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.consuming.EventLog;
import com.opencredo.concourse.domain.time.TimeUUID;

import java.util.*;
import java.util.function.UnaryOperator;

public final class SubBatchingEventBatch implements EventBatch {

    public static EventBatch writingTo(EventLog eventLog, int maxSubBatchSize) {
        return new SubBatchingEventBatch(eventLog, maxSubBatchSize);
    }

    private final UUID id = TimeUUID.timeBased();
    private final List<Event> events = new LinkedList<>();
    private final UnaryOperator<Collection<Event>> eventsConsumer;
    private final int maxSubBatchSize;

    private SubBatchingEventBatch(UnaryOperator<Collection<Event>> eventsConsumer, int maxSubBatchSize) {
        this.eventsConsumer = eventsConsumer;
        this.maxSubBatchSize = maxSubBatchSize;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void complete() {
        eventsConsumer.apply(events);
    }

    @Override
    public void accept(Event event) {
        events.add(event);
        if (events.size() == maxSubBatchSize) {
            eventsConsumer.apply(new ArrayList<>(events));
            events.clear();
        }
    }
}
