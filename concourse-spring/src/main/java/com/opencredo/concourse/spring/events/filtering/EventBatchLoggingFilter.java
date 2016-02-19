package com.opencredo.concourse.spring.events.filtering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.batching.EventBatch;
import com.opencredo.concourse.domain.events.filtering.EventBatchPostFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Filter
public final class EventBatchLoggingFilter implements EventBatchPostFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBatchLoggingFilter.class);

    @Override
    public void afterComplete(EventBatch eventBatch) {
        LOGGER.info("Batch {} completed", eventBatch.getId());
    }

    @Override
    public void afterAccept(EventBatch eventBatch, Event event) {
        LOGGER.debug("Batch {} received event {}", eventBatch.getId(), event);
    }
}
