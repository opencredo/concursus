package com.opencredo.concursus.domain.events.storage;

import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;
import com.opencredo.concursus.domain.time.TimeRange;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.opencredo.concursus.domain.events.selection.EventSelection.*;

/**
 * An in-memory {@link EventStore}, primarily for testing purposes.
 */
public final class InMemoryEventStore implements EventStore {

    private static final Comparator<Event> reverseTimestampOrder = Comparator.comparing(Event::getEventTimestamp)
            .reversed();

    public static InMemoryEventStore empty() {
        return new InMemoryEventStore(new ConcurrentHashMap<>());
    }

    public static InMemoryEventStore with(Collection<Event> events) {
        InMemoryEventStore eventStore = empty();
        eventStore.accept(events);
        return eventStore;
    }

    private final ConcurrentMap<AggregateId, Set<Event>> events;

    private InMemoryEventStore(ConcurrentMap<AggregateId, Set<Event>> events) {
        this.events = events;
    }

    @Override
    public void accept(Collection<Event> events) {
        events.forEach(this::store);
    }

    private void store(Event event) {
        events.compute(event.getAggregateId(),
                (id, events) -> {
                    Set<Event> updatedEvents = events == null ? new TreeSet<>(reverseTimestampOrder) : events;
                    updatedEvents.add(event);
                    return updatedEvents;
                });
    }

    @Override
    public List<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange) {
        return selectEvents(events, inRange(timeRange).and(matchedBy(matcher)), aggregateId);
    }

    @Override
    public Map<AggregateId, List<Event>> getEvents(EventTypeMatcher matcher, String aggregateType, Collection<String> aggregateIds, TimeRange timeRange) {
        return selectEvents(events, matchedBy(matcher).and(inRange(timeRange)), aggregateType, aggregateIds);
    }
}
