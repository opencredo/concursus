package com.opencredo.concourse.domain.events.sourcing;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Collection;

@FunctionalInterface
public interface EventSource {

    Collection<Event> getEvents(AggregateId aggregateId, TimeRange timeRange);

    default Collection<Event> getEvents(AggregateId aggregateId) {
        return getEvents(aggregateId, TimeRange.unbounded());
    }

}
