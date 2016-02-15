package com.opencredo.concourse.domain.events.storing;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public final class InMemoryEventStore implements EventStore {

    public static InMemoryEventStore empty() {
        return new InMemoryEventStore(new ConcurrentHashMap<>());
    }

    public static InMemoryEventStore with(ConcurrentMap<AggregateId, SortedSet<Event>> events) {
        return new InMemoryEventStore(events);
    }

    public static InMemoryEventStore with(Collection<Event> events) {
        InMemoryEventStore eventStore = empty();
        events.forEach(eventStore::store);
        return eventStore;
    }

    private final ConcurrentMap<AggregateId, SortedSet<Event>> events;

    private InMemoryEventStore(ConcurrentMap<AggregateId, SortedSet<Event>> events) {
        this.events = events;
    }

    @Override
    public void accept(Collection<Event> events) {
        events.forEach(this::store);
    }

    private void store(Event event) {
        events.compute(event.getAggregateId(),
                (id, events) -> {
                    SortedSet<Event> updatedEvents = events == null ? new TreeSet<>() : events;
                    updatedEvents.add(event);
                    return updatedEvents;
                });
    }

    @Override
    public Collection<Event> getEvents(AggregateId aggregateId, TimeRange timeRange) {
        return Optional.of(events.get(aggregateId))
                .map(filterByTimeRange(timeRange))
                .orElseGet(TreeSet::new);
    }

    @Override
    public EventSource preload(String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
        Set<AggregateId> aggregateIdSet = aggregateIds.stream()
                .map(id -> AggregateId.of(aggregateType, id))
                .collect(Collectors.toSet());

        return with(new ConcurrentHashMap<>(events.entrySet().stream()
                .filter(e -> aggregateIdSet.contains(e.getKey()))
                .collect(Collectors.toMap(
                        Entry::getKey,
                        filterByTimeRange(timeRange).compose(Entry::getValue)))));
    }

    private UnaryOperator<SortedSet<Event>> filterByTimeRange(TimeRange timeRange) {
        return timeRange.isUnbounded()
                ? events -> events
                : events -> events.stream()
                    .filter(event -> timeRange.contains(event.getEventTimestamp().getTimestamp()))
                    .collect(Collectors.toCollection(TreeSet::new));
    }
}
