package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.logging.EventLog;

import java.util.Collection;

public interface EventLogPreFilter extends ObservingEventLogFilter {

    @Override
    default Collection<Event> onLog(EventLog eventLog, Collection<Event> events) {
        Collection<Event> filtered = beforeLog(eventLog, events);
        return eventLog.apply(filtered);
    }

    Collection<Event> beforeLog(EventLog eventLog, Collection<Event> events);

}
