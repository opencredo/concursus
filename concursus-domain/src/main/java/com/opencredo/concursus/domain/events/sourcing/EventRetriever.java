package com.opencredo.concursus.domain.events.sourcing;

import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;
import com.opencredo.concursus.domain.time.TimeRange;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A means of retrieving {@link Event}s for a given aggregate or group of aggregates.
 */
public interface EventRetriever {

    /**
     * Fetch all of the {@link Event}s in the event history for the given {@link AggregateId} that are matched by the supplied {@link EventTypeMatcher} and fall within the given {@link TimeRange}.
     * @param matcher An {@link EventTypeMatcher} that provides {@link com.opencredo.concursus.data.tuples.TupleSchema}s for {@link com.opencredo.concursus.domain.events.EventType}s. Only events matched by this matcher will be returned.
     * @param aggregateId The {@link AggregateId} to retrieve events for.
     * @param timeRange The {@link TimeRange} to restrict returned events to.
     * @return The retrieved events.
     */
    List<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange);

    /**
     * Fetch all of the {@link Event}s in the event histories for the given aggregate ids that are matched by the supplied {@link EventTypeMatcher} and fall within the given {@link TimeRange}.
     * @param matcher An {@link EventTypeMatcher} that provides {@link com.opencredo.concursus.data.tuples.TupleSchema}s for {@link com.opencredo.concursus.domain.events.EventType}s. Only events matched by this matcher will be returned.
     * @param aggregateType The aggregate type to which all of the aggregate ids belong.
     * @param aggregateIds The aggregate ids to retrieve events for.
     * @param timeRange The {@link TimeRange} to restrict returned events to.
     * @return The retrieved events, grouped by {@link AggregateId}.
     */
    Map<AggregateId, List<Event>> getEvents(EventTypeMatcher matcher, String aggregateType, Collection<String> aggregateIds, TimeRange timeRange);

}
