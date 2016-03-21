package com.opencredo.concourse.domain.events.sourcing;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.List;

/**
 * An {@link Event} source that has pre-loaded events into a cache.
 */
@FunctionalInterface
public interface CachedEventSource {

    /**
     * Get the {@link Event}s for the given {@link AggregateId} within the given {@link TimeRange}.
     * @param aggregateId The {@link AggregateId} to get events for.
     * @param timeRange The {@link TimeRange} to restrict results to.
     * @return The retrieved {@link Event}s.
     */
    List<Event> getEvents(AggregateId aggregateId, TimeRange timeRange);

    /**
     * Get all {@link Event}s for the given {@link AggregateId}.
     * @param aggregateId The {@link AggregateId} to get events for.
     * @return The retrieved {@link Event}s.
     */
    default List<Event> getEvents(AggregateId aggregateId) {
        return getEvents(aggregateId, TimeRange.unbounded());
    }

    /**
     * Get an {@link EventReplayer} replaying events for the given {@link AggregateId} within the given {@link TimeRange}.
     * @param aggregateId The {@link AggregateId} to replay events for.
     * @param timeRange The {@link TimeRange} to restrict results to.
     * @return An {@link EventReplayer} for the retrieved {@link Event}s.
     */
    default EventReplayer replaying(AggregateId aggregateId, TimeRange timeRange) {
        return EventReplayer.of(getEvents(aggregateId, timeRange));
    }

    /**
     * Get an {@link EventReplayer} replaying all events for the given {@link AggregateId}.
     * @param aggregateId The {@link AggregateId} to replay events for.
     * @return An {@link EventReplayer} for the retrieved {@link Event}s.
     */
    default EventReplayer replaying(AggregateId aggregateId) {
        return replaying(aggregateId, TimeRange.unbounded());
    }

}
