package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.logging.EventLog;

import java.util.Collection;

@FunctionalInterface
public interface ObservingEventLogFilter extends EventLogFilter {

    @Override
    default EventLog apply(EventLog eventLog) {
        return events -> onLog(eventLog, events);
    }

    Collection<Event> onLog(EventLog eventLog, Collection<Event> events);

}
