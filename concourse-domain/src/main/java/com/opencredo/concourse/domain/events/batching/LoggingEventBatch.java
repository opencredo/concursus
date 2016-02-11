package com.opencredo.concourse.domain.events.batching;

import com.opencredo.concourse.domain.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class LoggingEventBatch implements EventBatch {

    public static EventBatch logging(EventBatch loggedEventBatch) {
        return new LoggingEventBatch(loggedEventBatch);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEventBatch.class);

    private final EventBatch loggedEventBatch;

    private LoggingEventBatch(EventBatch loggedEventBatch) {
        this.loggedEventBatch = loggedEventBatch;
    }

    @Override
    public UUID getId() {
        return loggedEventBatch.getId();
    }

    @Override
    public void complete() {
        LOGGER.info("Batch {} completed", getId());
        loggedEventBatch.complete();
    }

    @Override
    public void accept(Event event) {
        LOGGER.debug("Batch {} received event {}", getId(), event);
        loggedEventBatch.accept(event);
    }
}
