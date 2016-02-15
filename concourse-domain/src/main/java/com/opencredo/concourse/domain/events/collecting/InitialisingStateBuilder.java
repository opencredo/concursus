package com.opencredo.concourse.domain.events.collecting;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

class InitialisingStateBuilder<E, S> implements StateBuilder<E, S> {

    static <E, S> StateBuilder<E, S> using(Function<E, S> initialiser, BiFunction<E, S, S> mutator) {
        return new InitialisingStateBuilder<>(initialiser, s -> MutatingUpdatableState.using(s, mutator));
    }

    private final Function<E, S> initialiser;
    private final Function<S, UpdatableState<E, S>> updatableStateBuilder;
    private Optional<UpdatableState<E, S>> transformer = Optional.empty();

    private InitialisingStateBuilder(Function<E, S> initialiser, Function<S, UpdatableState<E, S>> updatableStateBuilder) {
        this.initialiser = initialiser;
        this.updatableStateBuilder = updatableStateBuilder;
    }

    @Override
    public void accept(E e) {
        if (transformer.isPresent()) {
            transformer.ifPresent(st -> st.accept(e));
        } else {
            transformer = Optional.of(updatableStateBuilder.apply(initialiser.apply(e)));
        }
    }

    @Override
    public Optional<S> get() {
        return transformer.map(UpdatableState::get);
    }

}
