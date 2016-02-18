package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.batching.EventBatch;
import com.opencredo.concourse.domain.events.dispatching.EventBus;

public interface EventBusPreFilter extends ObservingEventBusFilter {

    @Override
    default EventBatch onStartBatch(EventBus eventBus) {
        beforeStartBatch(eventBus);
        return eventBus.startBatch();
    }

    void beforeStartBatch(EventBus eventBus);

}