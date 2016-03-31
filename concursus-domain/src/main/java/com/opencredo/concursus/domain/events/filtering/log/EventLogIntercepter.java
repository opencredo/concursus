package com.opencredo.concursus.domain.events.filtering.log;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.logging.EventLog;

import java.util.Collection;

@FunctionalInterface
public interface EventLogIntercepter extends EventLogFilter {

    @Override
    default EventLog apply(EventLog eventLog) {
        return events -> onLog(eventLog, events);
    }

    Collection<Event> onLog(EventLog eventLog, Collection<Event> events);

}
