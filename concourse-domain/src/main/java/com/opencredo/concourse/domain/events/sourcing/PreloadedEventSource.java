package com.opencredo.concourse.domain.events.sourcing;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.List;

@FunctionalInterface
public interface PreloadedEventSource {

    List<Event> getEvents(AggregateId aggregateId, TimeRange timeRange);

    default List<Event> getEvents(AggregateId aggregateId) {
        return getEvents(aggregateId, TimeRange.unbounded());
    }

    default EventReplayer replaying(AggregateId aggregateId, TimeRange timeRange) {
        return EventReplayer.of(getEvents(aggregateId, timeRange));
    }

    default EventReplayer replaying(AggregateId aggregateId) {
        return replaying(aggregateId, TimeRange.unbounded());
    }

}
