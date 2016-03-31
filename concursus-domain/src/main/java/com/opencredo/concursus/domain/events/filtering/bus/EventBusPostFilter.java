package com.opencredo.concursus.domain.events.filtering.bus;

import com.opencredo.concursus.domain.events.batching.EventBatch;
import com.opencredo.concursus.domain.events.dispatching.EventBus;

@FunctionalInterface
public interface EventBusPostFilter extends EventBusIntercepter {

    @Override
    default EventBatch onStartBatch(EventBus eventBus) {
        return afterStartBatch(eventBus, eventBus.startBatch());
    }

    EventBatch afterStartBatch(EventBus eventBus, EventBatch eventBatch);

}
