package com.opencredo.concursus.spring.events.filtering.logging;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.filtering.log.EventLogPostFilter;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.spring.events.filtering.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Filter
public final class EventLogLoggingFilter implements EventLogPostFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventLogLoggingFilter.class);

    @Override
    public Collection<Event> afterLog(EventLog eventLog, Collection<Event> events) {
        LOGGER.info("Event log received {} events", events.size());
        return events;
    }
}
