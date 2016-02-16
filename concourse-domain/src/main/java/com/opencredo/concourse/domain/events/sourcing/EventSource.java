package com.opencredo.concourse.domain.events.sourcing;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.UUID;

public interface EventSource {

    NavigableSet<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange);
    PreloadedEventSource preload(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange);

    default NavigableSet<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId) {
        return getEvents(matcher, aggregateId, TimeRange.unbounded());
    }

    default EventReplayer replaying(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange) {
        return EventReplayer.of(getEvents(matcher, aggregateId, timeRange));
    }

    default EventReplayer replaying(EventTypeMatcher matcher, AggregateId aggregateId) {
        return replaying(matcher, aggregateId, TimeRange.unbounded());
    }

    default PreloadedEventSource preload(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds) {
        return preload(matcher, aggregateType, aggregateIds, TimeRange.unbounded());
    }

}
