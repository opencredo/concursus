package com.opencredo.concourse.domain.events.filtering.bus;

import com.opencredo.concourse.domain.events.batching.EventBatch;
import com.opencredo.concourse.domain.events.dispatching.EventBus;

@FunctionalInterface
public interface EventBusPreFilter extends EventBusIntercepter {

    @Override
    default EventBatch onStartBatch(EventBus eventBus) {
        beforeStartBatch(eventBus);
        return eventBus.startBatch();
    }

    void beforeStartBatch(EventBus eventBus);

}
