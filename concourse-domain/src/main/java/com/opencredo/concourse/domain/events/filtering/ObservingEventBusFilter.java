package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.batching.EventBatch;
import com.opencredo.concourse.domain.events.dispatching.EventBus;

public interface ObservingEventBusFilter extends EventBusFilter {

    @Override
    default EventBus apply(EventBus eventBus) {
        return () -> onStartBatch(eventBus);
    }

    EventBatch onStartBatch(EventBus eventBus);
}
