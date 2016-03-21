package com.opencredo.concourse.domain.events.sourcing;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.*;

import static com.opencredo.concourse.domain.events.selection.EventSelection.*;

class EventCache implements EventRetriever, CachedEventSource {

    static EventCache containing(Map<AggregateId, List<Event>> events) {
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
