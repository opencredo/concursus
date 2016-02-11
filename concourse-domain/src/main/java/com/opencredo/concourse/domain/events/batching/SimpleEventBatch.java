package com.opencredo.concourse.domain.events.batching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.time.TimeUUID;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class SimpleEventBatch implements EventBatch {

    public static EventBatch writingTo(Consumer<Collection<Event>> eventsConsumer) {
        return new SimpleEventBatch(eventsConsumer);
    }

    private final UUID id = TimeUUID.timeBased();
    private final List<Event> events = new LinkedList<>();
    private final Consumer<Collection<Event>> eventsConsumer;

    private SimpleEventBatch(Consumer<Collection<Event>> eventsConsumer) {
        this.eventsConsumer = eventsConsumer;
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
    }
}
