package com.opencredo.concursus.domain.events.sourcing;

import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;
import com.opencredo.concursus.domain.time.TimeRange;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

final class CachingEventSource implements EventSource {

    static CachingEventSource retrievingWith(EventRetriever eventRetriever) {
        checkNotNull(eventRetriever, "eventRetriever must not be null");

        return new CachingEventSource(eventRetriever);
    }

    private final EventRetriever eventRetriever;

    private CachingEventSource(EventRetriever eventRetriever) {
        this.eventRetriever = eventRetriever;
    }

    @Override
    public CachedEventSource preload(EventTypeMatcher matcher, String aggregateType, Collection<String> aggregateIds, TimeRange timeRange) {
        checkNotNull(matcher, "matcher must not be null");
        checkNotNull(aggregateType, "aggregateType must not be null");
        checkNotNull(aggregateIds, "aggregateIds must not be null");
        checkNotNull(timeRange, "timeRange must not be null");

        return EventCache.containing(eventRetriever.getEvents(matcher, aggregateType, aggregateIds, timeRange));
    }

    @Override
    public List<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange) {
        checkNotNull(matcher, "matcher must not be null");
        checkNotNull(aggregateId, "aggregateId must not be null");
        checkNotNull(timeRange, "timeRange must not be null");

        return eventRetriever.getEvents(matcher, aggregateId, timeRange);
    }
}
