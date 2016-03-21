package com.opencredo.concourse.domain.events.filtering.log;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.logging.EventLog;

import java.util.Collection;

@FunctionalInterface
public interface EventLogPostFilter extends EventLogIntercepter {

    @Override
    default Collection<Event> onLog(EventLog eventLog, Collection<Event> events) {
        return afterLog(eventLog, eventLog.apply(events));
    }

    Collection<Event> afterLog(EventLog eventLog, Collection<Event> events);
}
