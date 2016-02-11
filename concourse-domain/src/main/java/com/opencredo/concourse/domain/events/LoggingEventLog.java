package com.opencredo.concourse.domain.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingEventLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEventLog.class);

    static EventLog logging(EventLog loggedEventLog) {
        return events -> {
            LOGGER.info("Event log received {} events ", events.size());
            loggedEventLog.accept(events);
        };
    }

    private LoggingEventLog() {
    }
}
