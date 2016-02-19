package com.opencredo.concourse.domain.events.sourcing;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EventRetriever {

    List<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange);
    Map<AggregateId, List<Event>> getEvents(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange);

}
