package com.opencredo.concursus.domain.storing;

import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.sourcing.EventRetriever;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;
import com.opencredo.concursus.domain.persisting.EventPersister;
import com.opencredo.concursus.domain.time.TimeRange;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * An {@link EventStore} composed of an {@link EventPersister} and an {@link EventRetriever}.
 */
public final class ComposedEventStore implements EventStore {

    /**
     * Create a new {@link EventStore} composing the supplied {@link EventPersister} and {@link EventRetriever}.
     * @param eventPersister The {@link EventPersister} to persist events with.
     * @param eventRetriever The {@link EventRetriever} to retrieve events with.
     * @return The composed {@link EventStore}.
     */
    public static EventStore create(EventPersister eventPersister, EventRetriever eventRetriever) {
        return new ComposedEventStore(eventPersister, eventRetriever);
    }

    private final EventPersister eventPersister;
    private final EventRetriever eventRetriever;

    private ComposedEventStore(EventPersister eventPersister, EventRetriever eventRetriever) {
        this.eventPersister = eventPersister;
        this.eventRetriever = eventRetriever;
    }

    @Override
    public List<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange) {
        return eventRetriever.getEvents(matcher, aggregateId, timeRange);
    }

    @Override
    public Map<AggregateId, List<Event>> getEvents(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return eventRetriever.getEvents(matcher, aggregateType, aggregateIds, timeRange);
    }

    @Override
    public void accept(Collection<Event> events) {
        eventPersister.accept(events);
    }
}
