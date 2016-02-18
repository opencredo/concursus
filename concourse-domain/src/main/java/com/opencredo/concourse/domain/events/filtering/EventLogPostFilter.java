package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.logging.EventLog;

import java.util.Collection;

public interface EventLogPostFilter extends ObservingEventLogFilter {

    @Override
    default Collection<Event> onLog(EventLog eventLog, Collection<Event> events) {
        return afterLog(eventLog, eventLog.apply(events));
    }

    Collection<Event> afterLog(EventLog eventLog, Collection<Event> events);
}
