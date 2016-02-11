package com.opencredo.concourse.domain.events.collection;

import java.util.Optional;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class EventStreamCollector {

    public static <E, S> Collector<E, StateBuilder<E, S>, Optional<S>> toState(
            Function<E, S> initialiser,
            BiFunction<E, S, S> accumulator) {
        return accumulateAndGet(StateBuilder.using(initialiser, accumulator));
    }

    public static <E, S> Collector<E, UpdatableState<E, S>, S> toNewState(
            S initialState,
            BiFunction<E, S, S> accumulator) {
        return accumulateAndGet(UpdatableState.using(initialState, accumulator));
    }

    private static <E, O, T extends Accumulator<E, O>> Collector<E, T, O> accumulateAndGet(T accumulator) {
        return Collector.of(
                () -> accumulator,
                Consumer<E>::accept,
                (a1, a2) -> { throw new UnsupportedOperationException("Cannot merge parallel event streams"); },
                Supplier<O>::get
        );
    }

    public static <E, S> Optional<S> toState(Stream<E> events,
                                             Function<E, S> initialiser,
                                             BiFunction<E, S, S> accumulator) {
        return accumulateAndGet(events::forEach, StateBuilder.using(initialiser, accumulator));
    }

    public static <E, S> S toNewState(Stream<E> events,
                                             S initialState,
                                             BiFunction<E, S, S> accumulator) {
        return replayEventsTo(events::forEach, initialState, accumulator);
    }

    public static <E, S> Optional<S> replayEventsTo(Consumer<Consumer<E>> replayer, Function<E, S> initialiser, BiFunction<E, S, S> accumulator) {
        return accumulateAndGet(replayer, StateBuilder.using(initialiser, accumulator));
    }

    public static <E, S> S replayEventsTo(Consumer<Consumer<E>> replayer, S initialState, BiFunction<E, S, S> accumulator) {
        return accumulateAndGet(replayer, UpdatableState.using(initialState, accumulator));
    }

    private static <E, S, O> O accumulateAndGet(Consumer<Consumer<E>> replayer, Accumulator<E, O> accumulator) {
        replayer.accept(accumulator);
        return accumulator.get();
    }
}
