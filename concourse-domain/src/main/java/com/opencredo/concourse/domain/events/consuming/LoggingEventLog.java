package com.opencredo.concourse.domain.events.consuming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingEventLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEventLog.class);

    public static EventLog logging(EventLog loggedEventLog) {
        return events -> {
            LOGGER.info("Events consumer received {} events", events.size());
            return loggedEventLog.apply(events);
        };
    }
}
