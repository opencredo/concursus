package com.opencredo.concourse.mapping.events.methods.history;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.mapping.events.methods.reflection.EventInterfaceInfo;
import com.opencredo.concourse.domain.events.binding.EventTypeBinding;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EventHistoryFetcher {

    public static <T> EventHistoryFetcher of(Class<? extends T> eventInterface) {
        EventInterfaceInfo<T> interfaceInfo = EventInterfaceInfo.forInterface(eventInterface);

        return new EventHistoryFetcher(
                interfaceInfo.getEventTypeBinding(),
                interfaceInfo.getCausalOrderComparator());
    }

    private final EventTypeBinding eventTypeBinding;
    private final Comparator<Event> causalOrderComparator;

    private EventHistoryFetcher(EventTypeBinding eventTypeBinding, Comparator<Event> causalOrderComparator) {
        this.eventTypeBinding = eventTypeBinding;
        this.causalOrderComparator = causalOrderComparator;
    }

    public List<Event> getHistory(EventSource eventSource, UUID aggregateId) {
        return getHistory(eventSource, aggregateId, TimeRange.unbounded());
    }

    public List<Event> getHistory(EventSource eventSource, UUID aggregateId, TimeRange timeRange) {
        return eventTypeBinding.replaying(eventSource, aggregateId, timeRange)
                .inAscendingOrder(causalOrderComparator)
                .toList();
    }

    public List<Event> getHistory(CachedEventSource cachedEventSource, UUID aggregateId) {
        return getHistory(cachedEventSource, aggregateId, TimeRange.unbounded());
    }

    public List<Event> getHistory(CachedEventSource cachedEventSource, UUID aggregateId, TimeRange timeRange) {
        return eventTypeBinding.replaying(cachedEventSource, aggregateId, timeRange)
                .inAscendingOrder(causalOrderComparator)
                .toList();
    }

    public Map<UUID, List<Event>> getHistories(EventSource eventSource, Collection<UUID> aggregateIds) {
        return getHistories(eventSource, aggregateIds, TimeRange.unbounded());
    }

    public Map<UUID, List<Event>> getHistories(EventSource eventSource, Collection<UUID> aggregateIds, TimeRange timeRange) {
        CachedEventSource cachedEventSource = eventTypeBinding.preload(eventSource, aggregateIds, timeRange);
        return aggregateIds.stream().collect(Collectors.toMap(
                Function.identity(),
                id -> getHistory(cachedEventSource, id)
        ));
    }

}
