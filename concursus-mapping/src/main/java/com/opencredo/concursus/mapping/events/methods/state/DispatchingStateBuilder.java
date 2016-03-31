package com.opencredo.concursus.mapping.events.methods.state;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.state.StateBuilder;
import com.opencredo.concursus.mapping.events.methods.reflection.StateClassInfo;
import com.opencredo.concursus.mapping.events.methods.reflection.dispatching.EventDispatcher;
import com.opencredo.concursus.mapping.events.methods.reflection.dispatching.InitialEventDispatcher;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.opencredo.concursus.domain.events.EventCharacteristics.IS_INITIAL;

/**
 * {@link StateBuilder} that dispatches events to state-building methods on a given class/instance.
 * @param <T> The type of the state object to build.
 */
public final class DispatchingStateBuilder<T> implements StateBuilder<T> {

    /**
     * Return a {@link StateBuilder} that dispatches events to state-building methods on the given class.
     * @param stateClass The class to handle events with.
     * @param <T> The type of the class to handle events with.
     * @return The constructed {@link StateBuilder}
     */
    public static <T> StateBuilder<T> dispatchingTo(Class<? extends T> stateClass) {
        return dispatchingTo(stateClass, Optional.empty());
    }

    /**
     * Return a {@link StateBuilder} that dispatches events to state-building methods on the given instance.
     * @param stateInstance The instance to handle events with.
     * @param <T> The type of the instance to handle events with.
     * @return The constructed {@link StateBuilder}
     */
    @SuppressWarnings("unchecked")
    public static <T> StateBuilder<T> dispatchingTo(T stateInstance) {
        return dispatchingTo((Class<? extends T>) stateInstance.getClass(), stateInstance);
    }

    /**
     * Return a {@link StateBuilder} that dispatches events to state-building methods on the given instance.
     * @param stateClass The class to handle events with.
     * @param stateInstance The instance to handle events with.
     * @param <T> The type of the instance to handle events with.
     * @return The constructed {@link StateBuilder}
     */
    public static <T> StateBuilder<T> dispatchingTo(Class<? extends T> stateClass, T stateInstance) {
        return dispatchingTo(stateClass, Optional.ofNullable(stateInstance));
    }

    private static <T> StateBuilder<T> dispatchingTo(Class<? extends T> stateClass, Optional<T> stateInstance) {
        checkNotNull(stateClass, "stateClass must not be null");

        StateClassInfo<T> stateClassInfo = StateClassInfo.forStateClass(stateClass);

        return new DispatchingStateBuilder<>(
                stateClassInfo.getInitialEventDispatcher(),
                stateClassInfo.getUpdateEventDispatcher(),
                stateInstance);
    }

    /**
     * Create a {@link StateBuilder} that dispatches events using the supplied {@link InitialEventDispatcher} and {@link EventDispatcher}.
     * @param initialEventDispatcher The dispatcher to use to dispatch initial events.
     * @param updateMethodDispatcher The dispatcher to use to dispatch update events.
     * @param <T> The type of the state object to build.
     * @return The constructed {@link StateBuilder}
     */
    public static <T> StateBuilder<T> dispatching(InitialEventDispatcher<T> initialEventDispatcher, EventDispatcher<T> updateMethodDispatcher) {
        return new DispatchingStateBuilder<>(initialEventDispatcher, updateMethodDispatcher, Optional.empty());
    }

    private Optional<T> state;

    private final InitialEventDispatcher<T> initialEventDispatcher;
    private final EventDispatcher<T> updateMethodDispatcher;

    private DispatchingStateBuilder(InitialEventDispatcher<T> initialEventDispatcher, EventDispatcher<T> updateMethodDispatcher, Optional<T> state) {
        this.initialEventDispatcher = initialEventDispatcher;
        this.updateMethodDispatcher = updateMethodDispatcher;
        this.state = state;
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
