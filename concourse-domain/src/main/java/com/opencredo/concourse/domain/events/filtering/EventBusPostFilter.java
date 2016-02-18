package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.batching.EventBatch;
import com.opencredo.concourse.domain.events.dispatching.EventBus;

@FunctionalInterface
public interface EventBusPostFilter extends ObservingEventBusFilter {

    @Override
    default EventBatch onStartBatch(EventBus eventBus) {
        return afterStartBatch(eventBus, eventBus.startBatch());
    }

    EventBatch afterStartBatch(EventBus eventBus, EventBatch eventBatch);

}
