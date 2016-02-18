package com.opencredo.concourse.domain.events.caching;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.EventRetriever;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.events.sourcing.PreloadedEventSource;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An EventSource that preloads events into an {@link EventCache}
 */
public final class CachingEventSource implements EventSource {

    /**
     * Given an {@link EventRetriever}, return an {@link EventSource} that preloads events into an {@link EventCache}
     * @param eventRetriever The EventRetriever to use to retrieve events.
     * @return The caching EventSource
     */
    public static CachingEventSource retrievingWith(EventRetriever eventRetriever) {
        checkNotNull(eventRetriever, "eventRetriever must not be null");

        return new CachingEventSource(eventRetriever);
    }

    private final EventRetriever eventRetriever;

    private CachingEventSource(EventRetriever eventRetriever) {
        this.eventRetriever = eventRetriever;
    }

    @Override
    public PreloadedEventSource preload(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
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
