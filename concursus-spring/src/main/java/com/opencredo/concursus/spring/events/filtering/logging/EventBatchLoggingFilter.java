package com.opencredo.concursus.spring.events.filtering.logging;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.batching.EventBatch;
import com.opencredo.concursus.domain.events.filtering.batch.EventBatchPostFilter;
import com.opencredo.concursus.spring.events.filtering.Filter;
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
        LOGGER.info("Batch {} received event {}", eventBatch.getId(), event);
    }
}
