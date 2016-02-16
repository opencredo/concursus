package com.opencredo.concourse.domain.events.caching;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.consuming.EventLog;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.sourcing.PreloadedEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.domain.time.TimeUUID;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public final class InMemoryEventStore implements EventLog, EventSource, PreloadedEventSource {

    public static InMemoryEventStore empty() {
        return with(new ConcurrentHashMap<>());
    }

    public static InMemoryEventStore with(ConcurrentMap<AggregateId, NavigableSet<Event>> events) {
        return new InMemoryEventStore(events, EventCache.containing(events));
    }

    public static InMemoryEventStore with(Collection<Event> events) {
        InMemoryEventStore eventStore = empty();
        eventStore.apply(events);
        return eventStore;
    }

    private final ConcurrentMap<AggregateId, NavigableSet<Event>> events;
    private final EventCache eventCache;

    private InMemoryEventStore(ConcurrentMap<AggregateId, NavigableSet<Event>> events, EventCache eventCache) {
        this.events = events;
        this.eventCache = eventCache;
    }

    @Override
    public Collection<Event> apply(Collection<Event> events) {
        return events.stream().map(event -> {
            Event processed = event.processed(TimeUUID.timeBased());
            store(processed);
            return processed;
        }).collect(Collectors.toList());
    }

    private void store(Event event) {
        events.compute(event.getAggregateId(),
                (id, events) -> {
                    NavigableSet<Event> updatedEvents = events == null ? new TreeSet<>() : events;
                    updatedEvents.add(event);
                    return updatedEvents;
                });
    }

    @Override
    public NavigableSet<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange) {
        return eventCache.getEvents(matcher, aggregateId, timeRange);
    }

    @Override
    public PreloadedEventSource preload(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return EventCache.containing(eventCache.getEvents(matcher, aggregateType, aggregateIds, timeRange));
    }

    @Override
    public NavigableSet<Event> getEvents(AggregateId aggregateId, TimeRange timeRange) {
        return eventCache.getEvents(aggregateId, timeRange);
    }
}
