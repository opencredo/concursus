package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.EventReplayer;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.MultiTypeEventDispatcher;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Wraps an {@link EventReplayer} and dispatches replayed events to suitable handlers.
 * @param <T> The type of the event-emitter interface that the handlers must implement.
 */
public final class DispatchingEventReplayer<T> {

    static <T> DispatchingEventReplayer<T> dispatching(Comparator<Event> causalOrderComparator, MultiTypeEventDispatcher<T> mapper, EventReplayer eventReplayer) {
        return new DispatchingEventReplayer<>(causalOrderComparator, mapper, eventReplayer);
    }

    private final Comparator<Event> causalOrderComparator;
    private final MultiTypeEventDispatcher<T> dispatcher;
    private final EventReplayer eventReplayer;

    private DispatchingEventReplayer(Comparator<Event> causalOrderComparator, MultiTypeEventDispatcher<T> dispatcher, EventReplayer eventReplayer) {
        this.causalOrderComparator = causalOrderComparator;
        this.dispatcher = dispatcher;
        this.eventReplayer = eventReplayer;
    }

    /**
     * Set the order to descending.
     * @return A {@link DispatchingEventReplayer} replaying events in time-descending order.
     */
    public DispatchingEventReplayer<T> inDescendingOrder() {
        return new DispatchingEventReplayer<>(causalOrderComparator, dispatcher, eventReplayer.inDescendingOrder());
    }

    /**
     * Set the order to descending, sorted by the supplied {@link Comparator}.
     * @param comparator The {@link Comparator} comparator to use to sort events.
     * @return A {@link DispatchingEventReplayer} replaying events in time-descending order.
     */
    public DispatchingEventReplayer<T> inDescendingOrder(Comparator<Event> comparator) {
        return new DispatchingEventReplayer<>(causalOrderComparator, dispatcher, eventReplayer.inDescendingOrder(comparator));
    }

    /**
     * Set the order to descending.
     * @return A {@link DispatchingEventReplayer} replaying events in time-descending causal order.
     */
    public DispatchingEventReplayer<T> inDescendingCausalOrder() {
        return inDescendingOrder(causalOrderComparator);
    }

    /**
     * Set the order to ascending.
     * @return A {@link DispatchingEventReplayer} replaying events in time-ascending order.
     */
    public DispatchingEventReplayer<T> inAscendingOrder() {
        return new DispatchingEventReplayer<>(causalOrderComparator, dispatcher, eventReplayer.inAscendingOrder());
    }

    /**
     * Set the order to ascending, sorted by the supplied {@link Comparator}.
     * @param comparator The {@link Comparator} comparator to use to sort events.
     * @return A {@link DispatchingEventReplayer} replaying events in time-ascending order.
     */
    public DispatchingEventReplayer<T> inAscendingOrder(Comparator<Event> comparator) {
        return new DispatchingEventReplayer<>(causalOrderComparator, dispatcher, eventReplayer.inAscendingOrder(comparator));
    }

    /**
     * Set the order to ascending.
     * @return A {@link DispatchingEventReplayer} replaying events in time-ascending causal order.
     */
    public DispatchingEventReplayer<T> inAscendingCausalOrder() {
        return inAscendingOrder(causalOrderComparator);
    }

    /**
     * Apply a filter to the events
     * @param predicate The {@link Predicate} to use to filter events.
     * @return A {@link DispatchingEventReplayer} replaying events which match the filter.
     */
    public DispatchingEventReplayer<T> filter(Predicate<Event> predicate) {
        return new DispatchingEventReplayer<>(causalOrderComparator, dispatcher, eventReplayer.filter(predicate));
    }

    /**
     * Replay only the first {@link Event} in the sequence.
     * @param handler The event handler to replay the event to.
     */
    public void replayFirst(T handler) {
        eventReplayer.replayFirst(BoundEventDispatcher.binding(dispatcher, handler));
    }

    /**
     * Replay all of the {@link Event}s in the sequence.
     * @param handler The event handler to replay the events to.
     */
    public void replayAll(T handler) {
        eventReplayer.replayAll(BoundEventDispatcher.binding(dispatcher, handler));
    }

    /**
     * Collect only the first {@link Event} in the sequence.
     * @param collector The collector to collect the event with.
     * @param <V> The type of value returned by the collector.
     * @return The collected value, or {@link Optional}::empty if the sequence is empty.
     */
    public <V> Optional<V> collectFirst(Function<Consumer<V>, T> collector) {
        return eventReplayer.collectFirst(caller -> BoundEventDispatcher.binding(dispatcher, collector.apply(caller)));
    }

    /**
     * Collect all of the {@link Event}s in the sequence.
     * @param collector The collector to collect the events with.
     * @param <V> The type of value returned by the collector.
     * @return The collected values.
     */
    public <V> List<V> collectAll(Function<Consumer<V>, T> collector) {
        return eventReplayer.collectAll(caller -> BoundEventDispatcher.binding(dispatcher, collector.apply(caller)));
    }
}
