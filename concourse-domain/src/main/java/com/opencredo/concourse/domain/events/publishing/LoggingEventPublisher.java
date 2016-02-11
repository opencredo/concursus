package com.opencredo.concourse.domain.events.publishing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEventPublisher.class);

    public static EventPublisher logging(EventPublisher eventPublisher) {
        return event -> {
            LOGGER.debug("Publishing event {}", event);
            eventPublisher.accept(event);
        };
    }
}
