package com.opencredo.concourse.domain.events.batching;

import com.opencredo.concourse.domain.events.Event;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface EventBatch extends Consumer<Event> {

    UUID getId();
    void complete();

    default EventBatch filter(UnaryOperator<EventBatch> filter) {
        return filter.apply(this);
    }

}
