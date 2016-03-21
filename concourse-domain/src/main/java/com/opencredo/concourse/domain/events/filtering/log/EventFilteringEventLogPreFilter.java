package com.opencredo.concourse.domain.events.filtering.log;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.filtering.log.EventLogIntercepter;
import com.opencredo.concourse.domain.events.logging.EventLog;

import java.util.Collection;
import java.util.stream.Collectors;

@FunctionalInterface
public interface EventFilteringEventLogPreFilter extends EventLogIntercepter {

    @Override
    default Collection<Event> onLog(EventLog eventLog, Collection<Event> events) {
        return eventLog.apply(events.stream().filter(this::filterEvent).collect(Collectors.toList()));
    }

    boolean filterEvent(Event event);
}
