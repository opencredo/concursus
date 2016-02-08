package com.opencredo.concourse.domain.events;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface StateBuilder<E, S> extends Accumulator<E, Optional<S>> {

    static <E, S> StateBuilder<E, S> using(S state, BiFunction<E, S, S> mutator) {
        return InitialisedStateBuilder.using(state, mutator);
    }

    static <E, S> StateBuilder<E, S> using(Function<E, S> initialiser, BiFunction<E, S, S> mutator) {
        return InitialisingStateBuilder.using(initialiser, mutator);
    }

}
