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

public final class EventReplayer {

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

    public EventReplayer inAscendingOrder() {
        return new EventReplayer(events, this::reverseStream, filter);
    }

    public EventReplayer inAscendingOrder(Comparator<Event> comparator) {
        return new EventReplayer(events, eventList -> reverseStream(eventList).sorted(comparator), filter);
    }

    public EventReplayer inDescendingOrder() {
        return new EventReplayer(events, List::stream, filter);
    }

    public EventReplayer inDescendingOrder(Comparator<Event> comparator) {
        return new EventReplayer(events, eventList -> eventList.stream().sorted(comparator.reversed()), filter);
    }

    private Stream<Event> reverseStream(List<Event> eventList) {
        return StreamSupport.stream(ReverseListSpliterator.over(eventList), false);
    }

    public EventReplayer filter(Predicate<Event> predicate) {
        return new EventReplayer(events, orderedStreamer, Optional.of(predicate));
    }

    public void replayFirst(Consumer<Event> consumer) {
        stream().findFirst().ifPresent(consumer);
    }

    private Stream<Event> stream() {
        return filter.map(orderedStreamer.apply(events)::filter)
                .orElseGet(() -> orderedStreamer.apply(events));
    }

    public void replayAll(Consumer<Event> consumer) {
        stream().forEach(consumer);
    }

    public <T> Optional<T> collectFirst(Function<Consumer<T>, Consumer<Event>> consumerBuilder) {
        AtomicReference<T> ref = new AtomicReference<>();
        Consumer<Event> consumer = consumerBuilder.apply(ref::set);
        replayFirst(consumer);
        return Optional.ofNullable(ref.get());
    }

    public <T> List<T> collectAll(Function<Consumer<T>, Consumer<Event>> consumerBuilder) {
        List<T> result = new ArrayList<>();
        Consumer<Event> consumer = consumerBuilder.apply(result::add);
        replayAll(consumer);
        return result;
    }

    public List<Event> toList() {
        return stream().collect(Collectors.toList());
    }

}
