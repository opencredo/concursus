package com.opencredo.concourse.domain.events.caching;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.events.sourcing.EventRetriever;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.domain.time.TimeUUID;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static com.opencredo.concourse.domain.events.caching.EventSelection.*;

public final class InMemoryEventStore implements EventLog, EventRetriever {

    private static final Comparator<Event> reverseTimestampOrder = Comparator.comparing(Event::getEventTimestamp)
            .reversed();

    public static InMemoryEventStore empty() {
        return new InMemoryEventStore(new ConcurrentHashMap<>());
    }

    public static InMemoryEventStore with(Collection<Event> events) {
        InMemoryEventStore eventStore = empty();
        eventStore.apply(events);
        return eventStore;
    }

    private final ConcurrentMap<AggregateId, Set<Event>> events;

    private InMemoryEventStore(ConcurrentMap<AggregateId, Set<Event>> events) {
        this.events = events;
    }

    public EventSource getEventSource() {
        return CachingEventSource.retrievingWith(this);
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
    public Map<AggregateId, List<Event>> getEvents(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return selectEvents(events, matchedBy(matcher).and(inRange(timeRange)), aggregateType, aggregateIds);
    }
}
