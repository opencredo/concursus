package com.opencredo.concourse.domain.events.collecting;

import java.util.function.BiFunction;

public interface UpdatableState<E, S> extends Accumulator<E, S> {

    static <E, S> UpdatableState<E, S> using(S initialState, BiFunction<E, S, S> mutator) {
        return MutatingUpdatableState.using(initialState, mutator);
    }

}
