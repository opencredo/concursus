package com.opencredo.concursus.domain.events.selection;

import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;
import com.opencredo.concursus.domain.time.TimeRange;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class providing methods for selecting {@link Event}s matching various criteria.
 */
public final class EventSelection {

    private EventSelection() {
    }

    public static Function<Collection<Event>, List<Event>> filterBy(Predicate<Event> predicate) {
        return events -> events.stream().filter(predicate).collect(Collectors.toCollection(LinkedList::new));
    }

    public static Predicate<Event> inRange(TimeRange timeRange) {
        return event -> timeRange.contains(event.getEventTimestamp().getTimestamp());
    }

    public static Predicate<Event> matchedBy(EventTypeMatcher matcher) {
        return event -> matcher.match(event.getType()).isPresent();
    }

    public static List<Event> selectEvents(Map<AggregateId, ? extends Collection<Event>> events, Predicate<Event> filter, AggregateId aggregateId) {
        return Optional.ofNullable(events.get(aggregateId))
                .map(filterBy(filter))
                .orElseGet(Collections::emptyList);
    }

    public static Map<AggregateId, List<Event>> selectEvents(Map<AggregateId, ? extends Collection<Event>> events, Predicate<Event> filter, String aggregateType, Collection<UUID> aggregateIds) {
        Set<AggregateId> aggregateIdSet = aggregateIds.stream()
                .map(id -> AggregateId.of(aggregateType, id))
                .collect(Collectors.toSet());

        return events.entrySet().stream()
                .filter(e -> aggregateIdSet.contains(e.getKey()))
                .collect(Collectors.toMap(
                        Entry::getKey,
                        filterBy(filter).compose(Entry::getValue)));
    }
}
