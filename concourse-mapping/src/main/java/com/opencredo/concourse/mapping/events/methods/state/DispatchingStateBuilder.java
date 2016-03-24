package com.opencredo.concourse.mapping.events.methods.state;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.state.StateBuilder;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.EventDispatcher;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.InitialEventDispatcher;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.opencredo.concourse.domain.events.EventCharacteristics.IS_INITIAL;

final class DispatchingStateBuilder<T> implements StateBuilder<T> {

    public static <T> DispatchingStateBuilder<T> dispatching(InitialEventDispatcher<T> initialEventDispatcher, EventDispatcher<T> updateMethodDispatcher) {
        return new DispatchingStateBuilder<>(initialEventDispatcher, updateMethodDispatcher);
    }

    private Optional<T> state = Optional.empty();

    private final InitialEventDispatcher<T> initialEventDispatcher;
    private final EventDispatcher<T> updateMethodDispatcher;

    private DispatchingStateBuilder(InitialEventDispatcher<T> initialEventDispatcher, EventDispatcher<T> updateMethodDispatcher) {
        this.initialEventDispatcher = initialEventDispatcher;
        this.updateMethodDispatcher = updateMethodDispatcher;
    }

    @Override
    public void accept(Event event) {
        checkNotNull(event, "event must not be null");

        if (!state.isPresent() && event.hasCharacteristic(IS_INITIAL)) {
            state = Optional.of(initialEventDispatcher.apply(event));
        } else if (!event.hasCharacteristic(IS_INITIAL)) {
            state.ifPresent(s -> updateMethodDispatcher.accept(s, event));
        }
    }

    @Override
    public Optional<T> get() {
        return state;
    }
}
