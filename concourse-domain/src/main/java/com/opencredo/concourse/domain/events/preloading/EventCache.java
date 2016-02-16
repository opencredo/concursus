package com.opencredo.concourse.domain.events.preloading;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.domain.events.sourcing.EventRetriever;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class EventCache implements EventRetriever, EventSource {

    public static EventCache containing(Map<AggregateId, NavigableSet<Event>> events) {
        return new EventCache(events);
    }

    private final Map<AggregateId, NavigableSet<Event>> events;

    private EventCache(Map<AggregateId, NavigableSet<Event>> events) {
        this.events = events;
    }

    @Override
    public NavigableSet<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange) {
        return Optional.ofNullable(events.get(aggregateId))
                .map(filterBy(inRange(timeRange).and(matchedBy(matcher))))
                .orElseGet(TreeSet::new);
    }

    @Override
    public NavigableSet<Event> getEvents(AggregateId aggregateId, TimeRange timeRange) {
        return Optional.ofNullable(events.get(aggregateId))
                .map(filterBy(inRange(timeRange)))
                .orElseGet(TreeSet::new);
    }

    @Override
    public Map<AggregateId, NavigableSet<Event>> getEvents(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return preload(inRange(timeRange).and(matchedBy(matcher)), aggregateType, aggregateIds);
    }

    private UnaryOperator<NavigableSet<Event>> filterBy(Predicate<Event> predicate) {
        return events -> events.stream().filter(predicate).collect(Collectors.toCollection(TreeSet::new));
    }

    private Predicate<Event> inRange(TimeRange timeRange) {
        return event -> timeRange.contains(event.getEventTimestamp().getTimestamp());
    }

    private Predicate<Event> matchedBy(EventTypeMatcher matcher) {
        return event -> matcher.match(EventType.of(event)).isPresent();
    }

    private Map<AggregateId, NavigableSet<Event>> preload(Predicate<Event> filter, String aggregateType, Collection<UUID> aggregateIds) {
        Set<AggregateId> aggregateIdSet = aggregateIds.stream()
                .map(id -> AggregateId.of(aggregateType, id))
                .collect(Collectors.toSet());

        return events.entrySet().stream()
                .filter(e -> aggregateIdSet.contains(e.getKey()))
                .collect(Collectors.toMap(
                        Entry::getKey,
                        filterBy(filter).compose(Map.Entry::getValue)));
    }

    public Map<AggregateId,NavigableSet<Event>> getEvents(String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return preload(inRange(timeRange), aggregateType, aggregateIds);
    }
}
