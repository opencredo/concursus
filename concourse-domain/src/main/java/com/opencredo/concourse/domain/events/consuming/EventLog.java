package com.opencredo.concourse.domain.events.consuming;

import com.opencredo.concourse.domain.events.Event;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@FunctionalInterface
public interface EventLog extends UnaryOperator<Collection<Event>> {

    static EventLog of(EventLog eventLog) {
        return eventLog;
    }

    default EventLog publishingTo(Consumer<Event> eventPublisher) {
        return events -> {
            Collection<Event> processed = apply(events);
            processed.forEach(eventPublisher);
            return processed;
        };
    }

}
