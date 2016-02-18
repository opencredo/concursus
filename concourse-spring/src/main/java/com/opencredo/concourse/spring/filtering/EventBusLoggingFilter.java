package com.opencredo.concourse.spring.filtering;

import com.opencredo.concourse.domain.events.batching.EventBatch;
import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.events.filtering.EventBusPostFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Filter
public final class EventBusLoggingFilter implements EventBusPostFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBusLoggingFilter.class);

    @Override
    public EventBatch afterStartBatch(EventBus eventBus, EventBatch eventBatch) {
        LOGGER.info("Started batch {}", eventBatch.getId());
        return eventBatch;
    }
}
