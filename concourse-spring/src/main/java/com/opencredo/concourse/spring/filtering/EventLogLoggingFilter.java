package com.opencredo.concourse.spring.filtering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.filtering.EventLogPostFilter;
import com.opencredo.concourse.domain.events.logging.EventLog;
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
