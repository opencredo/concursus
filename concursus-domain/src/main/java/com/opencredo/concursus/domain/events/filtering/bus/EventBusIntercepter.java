package com.opencredo.concursus.domain.events.filtering.bus;

import com.opencredo.concursus.domain.events.batching.EventBatch;
import com.opencredo.concursus.domain.events.dispatching.EventBus;

@FunctionalInterface
public interface EventBusIntercepter extends EventBusFilter {

    @Override
    default EventBus apply(EventBus eventBus) {
        return () -> onStartBatch(eventBus);
    }

    EventBatch onStartBatch(EventBus eventBus);
}
