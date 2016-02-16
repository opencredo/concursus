package com.opencredo.concourse.domain.events.preloading;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.EventRetriever;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.UUID;

public final class CachingPreloadableEventSource implements PreloadableEventSource {

    private final EventRetriever eventRetriever;

    public CachingPreloadableEventSource(EventRetriever eventRetriever) {
        this.eventRetriever = eventRetriever;
    }

    @Override
    public EventSource preload(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return EventCache.containing(eventRetriever.getEvents(matcher, aggregateType, aggregateIds, timeRange));
    }

    @Override
    public NavigableSet<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange) {
        return eventRetriever.getEvents(matcher, aggregateId, timeRange);
    }
}
