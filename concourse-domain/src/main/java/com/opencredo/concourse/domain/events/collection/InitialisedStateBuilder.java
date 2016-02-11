package com.opencredo.concourse.domain.events.collection;

import java.util.Optional;
import java.util.function.BiFunction;

class InitialisedStateBuilder<E, S> implements StateBuilder<E, S> {

    static <E, S> StateBuilder<E, S> using(S initialState, BiFunction<E, S, S> mutator) {
        return new InitialisedStateBuilder<>(MutatingUpdatableState.using(initialState, mutator));
    }

    private InitialisedStateBuilder(UpdatableState<E, S> transformer) {
        this.transformer = transformer;
    }

    private final UpdatableState<E, S> transformer;


    @Override
    public void accept(E e) {
        transformer.accept(e);
    }

    @Override
    public Optional<S> get() {
        return Optional.of(transformer.get());
    }

}
