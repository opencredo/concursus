package com.opencredo.concourse.domain.events.consuming;

import com.opencredo.concourse.domain.events.Event;

import java.util.Collection;
import java.util.function.Consumer;

@FunctionalInterface
public interface EventLog extends Consumer<Collection<Event>> {

    static EventLog of(EventLog eventLog) {
        return eventLog;
    }

    default EventLog publishingTo(Consumer<Event> eventPublisher) {
        return andThen(events -> events.forEach(eventPublisher))::accept;
    }

}
