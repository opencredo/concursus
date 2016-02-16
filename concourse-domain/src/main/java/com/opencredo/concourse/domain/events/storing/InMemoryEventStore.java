package com.opencredo.concourse.domain.events.storing;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.consuming.EventLog;
import com.opencredo.concourse.domain.events.preloading.EventCache;
import com.opencredo.concourse.domain.events.preloading.PreloadableEventSource;
import com.opencredo.concourse.domain.events.preloading.TypeMatchedPreloadableEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryEventStore implements EventLog, PreloadableEventSource, TypeMatchedPreloadableEventSource {

    public static InMemoryEventStore empty() {
        return with(new ConcurrentHashMap<>());
    }

    public static InMemoryEventStore with(ConcurrentMap<AggregateId, NavigableSet<Event>> events) {
        return new InMemoryEventStore(events, EventCache.containing(events));
    }

    public static InMemoryEventStore with(Collection<Event> events) {
        InMemoryEventStore eventStore = empty();
        eventStore.accept(events);
        return eventStore;
    }

    private final ConcurrentMap<AggregateId, NavigableSet<Event>> events;
    private final EventCache eventCache;

    private InMemoryEventStore(ConcurrentMap<AggregateId, NavigableSet<Event>> events, EventCache eventCache) {
        this.events = events;
        this.eventCache = eventCache;
    }

    @Override
    public void accept(Collection<Event> events) {
        events.forEach(this::store);
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
    public EventSource preload(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return EventCache.containing(eventCache.getEvents(matcher, aggregateType, aggregateIds, timeRange));
    }

    @Override
    public EventSource preload(String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return EventCache.containing(eventCache.getEvents(aggregateType, aggregateIds, timeRange));
    }

    @Override
    public NavigableSet<Event> getEvents(AggregateId aggregateId, TimeRange timeRange) {
        return eventCache.getEvents(aggregateId, timeRange);
    }
}
