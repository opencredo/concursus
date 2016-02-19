package com.opencredo.concourse.domain.events.sourcing;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EventSource {

    List<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange);
    CachedEventSource preload(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange);

    default List<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId) {
        return getEvents(matcher, aggregateId, TimeRange.unbounded());
    }

    default EventReplayer replaying(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange) {
        return EventReplayer.of(getEvents(matcher, aggregateId, timeRange));
    }

    default EventReplayer replaying(EventTypeMatcher matcher, AggregateId aggregateId) {
        return replaying(matcher, aggregateId, TimeRange.unbounded());
    }

    default CachedEventSource preload(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds) {
        return preload(matcher, aggregateType, aggregateIds, TimeRange.unbounded());
    }

}
