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

    public static EventReplayer of(NavigableSet<Event> events) {
        return new EventReplayer(events, NavigableSet::stream, Optional.empty());
    }

    private final NavigableSet<Event> events;
    private final Function<NavigableSet<Event>, Stream<Event>> orderedStreamer;
    private final Optional<Predicate<Event>> filter;

    private EventReplayer(NavigableSet<Event> events, Function<NavigableSet<Event>, Stream<Event>> orderedStreamer, Optional<Predicate<Event>> filter) {
        this.events = events;
        this.orderedStreamer = orderedStreamer;
        this.filter = filter;
    }

    public EventReplayer inOrder(Comparator<Event> comparator) {
        return new EventReplayer(events, navigableSet -> navigableSet.stream().sorted(comparator), filter);
    }

    public EventReplayer inReverseOrder() {
        return new EventReplayer(events, this::reverseStream, filter);
    }

    private Stream<Event> reverseStream(NavigableSet<Event> navigableSet) {
        return StreamSupport.stream(
                Spliterators.spliterator(navigableSet.descendingIterator(), navigableSet.size(),
                Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SORTED | Spliterator.DISTINCT | Spliterator.NONNULL), false);
    }

    public EventReplayer inReverseOrder(Comparator<Event> comparator) {
        return new EventReplayer(events, deque -> reverseStream(deque).sorted(comparator.reversed()), filter);
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
