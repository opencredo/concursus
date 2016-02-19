package com.opencredo.concourse.domain.events.caching;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.EventRetriever;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.*;

import static com.opencredo.concourse.domain.events.caching.EventSelection.*;

class EventCache implements EventRetriever, CachedEventSource {

    public static EventCache containing(Map<AggregateId, List<Event>> events) {
        return new EventCache(events);
    }

    private final Map<AggregateId, List<Event>> events;

    private EventCache(Map<AggregateId, List<Event>> events) {
        this.events = events;
    }

    @Override
    public List<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange) {
        return selectEvents(events, inRange(timeRange).and(matchedBy(matcher)), aggregateId);
    }

    @Override
    public List<Event> getEvents(AggregateId aggregateId, TimeRange timeRange) {
        return selectEvents(events, inRange(timeRange), aggregateId);
    }

    @Override
    public Map<AggregateId, List<Event>> getEvents(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return selectEvents(events, inRange(timeRange).and(matchedBy(matcher)), aggregateType, aggregateIds);
    }

}
