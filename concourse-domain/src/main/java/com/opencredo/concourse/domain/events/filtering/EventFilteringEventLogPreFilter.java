package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.logging.EventLog;

import java.util.Collection;
import java.util.stream.Collectors;

public interface EventFilteringEventLogPreFilter extends EventLogPreFilter {

    @Override
    default Collection<Event> onLog(EventLog eventLog, Collection<Event> events) {
        return eventLog.apply(events.stream().filter(this::filterEvent).collect(Collectors.toList()));
    }

    boolean filterEvent(Event event);
}
