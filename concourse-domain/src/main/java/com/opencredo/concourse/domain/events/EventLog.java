package com.opencredo.concourse.domain.events;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface EventLog extends EventsConsumer {

    default EventLog filter(UnaryOperator<EventLog> filter) {
        return filter.apply(this);
    }

}
