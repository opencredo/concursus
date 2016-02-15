package com.opencredo.concourse.domain.events;

import com.opencredo.concourse.domain.events.batching.EventBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingEventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEventBus.class);

    public static EventBus logging(EventBus loggedEventBus) {
        return () -> {
            EventBatch batch = loggedEventBus.startBatch();
            LOGGER.info("Started batch {}", batch.getId());
            return batch;
        };
    }

    private LoggingEventBus() {
    }
}
