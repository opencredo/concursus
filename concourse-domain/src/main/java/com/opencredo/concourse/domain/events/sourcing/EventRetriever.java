package com.opencredo.concourse.domain.events.sourcing;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Collection;
import java.util.Map;
import java.util.NavigableSet;
import java.util.UUID;

public interface EventRetriever {

    NavigableSet<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange);
    Map<AggregateId, NavigableSet<Event>> getEvents(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange);

}
