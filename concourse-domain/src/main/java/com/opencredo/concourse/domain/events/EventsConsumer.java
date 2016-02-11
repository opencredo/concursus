package com.opencredo.concourse.domain.events;

import java.util.Collection;
import java.util.function.Consumer;

@FunctionalInterface
public interface EventsConsumer extends Consumer<Collection<Event>> {

    default EventsConsumer andPublish(Consumer<Event> eventPublisher) {
        return andThen(events -> events.forEach(eventPublisher))::accept;
    }

}
