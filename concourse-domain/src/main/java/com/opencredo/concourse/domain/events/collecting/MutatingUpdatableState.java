package com.opencredo.concourse.domain.events.collecting;

import java.util.function.BiFunction;

final class MutatingUpdatableState<E, S> implements UpdatableState<E, S> {

    static <E, S> MutatingUpdatableState<E, S> using(S initialState, BiFunction<E, S, S> mutator) {
        return new MutatingUpdatableState<>(initialState, mutator);
    }

    private S currentState;
    private final BiFunction<E, S, S> mutator;

    private MutatingUpdatableState(S currentState, BiFunction<E, S, S> mutator) {
        this.currentState = currentState;
        this.mutator = mutator;
    }

    @Override
    public void accept(E e) {
        currentState = mutator.apply(e, currentState);
    }

    @Override
    public S get() {
        return currentState;
    }
}
