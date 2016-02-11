package com.opencredo.concourse.domain.events.publishing;

import com.opencredo.concourse.domain.events.Event;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@FunctionalInterface
public interface EventPublisher extends Consumer<Event> {

    default EventPublisher filter(UnaryOperator<EventPublisher> filter) {
        return filter.apply(this);
    }

}
