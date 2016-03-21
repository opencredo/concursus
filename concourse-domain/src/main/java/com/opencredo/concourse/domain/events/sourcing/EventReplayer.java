package com.opencredo.concourse.domain.events.sourcing;

import com.opencredo.concourse.domain.events.Event;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Organises a collection of {@link Event}s for replaying or collection in the desired order.
 */
public final class EventReplayer {

    /**
     * Create an {@link EventReplayer} replaying the supplied {@link Event}s.
     * @param events The {@link Event}s to replay.
     * @return The constructed {@link EventReplayer}.
     */
    public static EventReplayer of(List<Event> events) {
        return new EventReplayer(events, List::stream, Optional.empty());
    }

    private final List<Event> events;
    private final Function<List<Event>, Stream<Event>> orderedStreamer;
    private final Optional<Predicate<Event>> filter;

    private EventReplayer(List<Event> events, Function<List<Event>, Stream<Event>> orderedStreamer, Optional<Predicate<Event>> filter) {
        this.events = events;
        this.orderedStreamer = orderedStreamer;
        this.filter = filter;
    }

    /**
     * Set the order to ascending.
     * @return An {@link EventReplayer} replaying events in time-ascending order.
     */
    public EventReplayer inAscendingOrder() {
        return new EventReplayer(events, this::reverseStream, filter);
    }

    /**
     * Set the order to ascending, sorted by the supplied {@link Comparator}.
     * @param comparator The {@link Comparator} comparator to use to sort events.
     * @return An {@link EventReplayer} replaying events in time-ascending order.
     */
    public EventReplayer inAscendingOrder(Comparator<Event> comparator) {
        return new EventReplayer(events, eventList -> reverseStream(eventList).sorted(comparator), filter);
    }

    /**
     * Set the order to descending.
     * @return An {@link EventReplayer} replaying events in time-descending order.
     */
    public EventReplayer inDescendingOrder() {
        return new EventReplayer(events, List::stream, filter);
    }

    /**
     * Set the order to descending, sorted by the supplied {@link Comparator}.
     * @param comparator The {@link Comparator} comparator to use to sort events.
     * @return An {@link EventReplayer} replaying events in time-descending order.
     */
    public EventReplayer inDescendingOrder(Comparator<Event> comparator) {
        return new EventReplayer(events, eventList -> eventList.stream().sorted(comparator.reversed()), filter);
    }

    private Stream<Event> reverseStream(List<Event> eventList) {
        return StreamSupport.stream(ReverseListSpliterator.over(eventList), false);
    }

    /**
     * Apply a filter to the events
     * @param predicate The {@link Predicate} to use to filter events.
     * @return An {@link EventReplayer} replaying events which match the filter.
     */
    public EventReplayer filter(Predicate<Event> predicate) {
        return new EventReplayer(events, orderedStreamer, Optional.of(predicate));
    }

    /**
     * Replay only the first {@link Event} in the sequence.
     * @param consumer The event consumer to replay the event to.
     */
    public void replayFirst(Consumer<Event> consumer) {
        stream().findFirst().ifPresent(consumer);
    }

    private Stream<Event> stream() {
        return filter.map(orderedStreamer.apply(events)::filter)
                .orElseGet(() -> orderedStreamer.apply(events));
    }

    /**
     * Replay all of the {@link Event}s in the sequence.
     * @param consumer The event consumer to replay the events to.
     */
    public void replayAll(Consumer<Event> consumer) {
        stream().forEach(consumer);
    }

    /**
     * Collect only the first {@link Event} in the sequence.
     * @param collector The collector to collect the event with.
     * @param <T> The type of value returned by the collector.
     * @return The collected value, or {@link Optional}::empty if the sequence is empty.
     */
    public <T> Optional<T> collectFirst(Function<Consumer<T>, Consumer<Event>> collector) {
        AtomicReference<T> ref = new AtomicReference<>();
        Consumer<Event> consumer = collector.apply(ref::set);
        replayFirst(consumer);
        return Optional.ofNullable(ref.get());
    }

    /**
     * Collect all of the {@link Event}s in the sequence.
     * @param collector The collector to collect the events with.
     * @param <T> The type of value returned by the collector.
     * @return The collected values.
     */
    public <T> List<T> collectAll(Function<Consumer<T>, Consumer<Event>> collector) {
        List<T> result = new ArrayList<>();
        Consumer<Event> consumer = collector.apply(result::add);
        replayAll(consumer);
        return result;
    }

    /**
     * Collect all of the {@link Event}s in the sequence into a {@link List}.
     * @return The collected events.
     */
    public List<Event> toList() {
        return stream().collect(Collectors.toList());
    }

}
