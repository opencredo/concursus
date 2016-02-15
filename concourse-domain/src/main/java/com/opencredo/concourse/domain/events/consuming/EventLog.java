package com.opencredo.concourse.domain.events.consuming;

import com.opencredo.concourse.domain.events.Event;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@FunctionalInterface
public interface EventLog extends Consumer<Collection<Event>> {

    static EventLog of(EventLog eventLog) {
        return eventLog;
    }

    default EventLog filter(UnaryOperator<EventLog> filter) {
        return filter.apply(this);
    }

    default EventLog publishingTo(Consumer<Event> eventPublisher) {
        return andThen(events -> events.forEach(eventPublisher))::accept;
    }

}
