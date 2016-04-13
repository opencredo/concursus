package com.opencredo.concursus.mapping.events.methods.state;

import com.opencredo.concursus.domain.events.sourcing.EventSource;
import com.opencredo.concursus.domain.events.state.StateBuilder;
import com.opencredo.concursus.domain.events.state.EventSourcingStateRepository;
import com.opencredo.concursus.domain.events.state.StateRepository;
import com.opencredo.concursus.mapping.events.methods.reflection.StateClassInfo;

import java.util.function.Supplier;

/**
 * Utility class which constructs a {@link StateRepository} which uses a {@link DispatchingStateBuilder} to construct
 * state objects.
 */
public final class DispatchingStateRepository {

    private DispatchingStateRepository() {
    }

    /**
     * Create a {@link StateRepository}, drawing on the supplied {@link EventSource}, which uses a {@link DispatchingStateBuilder}
     * to construct state objects of the given class.
     * @param eventSource The {@link EventSource} to retrieve events from.
     * @param stateClass The class of the state objects to construct.
     * @param <T> The type of the state objects to construct.
     * @return The constructed {@link StateRepository}.
     */
    public static <T> StateRepository<T> using(EventSource eventSource, Class<? extends T> stateClass) {
        StateClassInfo<T> stateClassInfo = StateClassInfo.forStateClass(stateClass);

        final Supplier<StateBuilder<T>> aggregateStateBuilderSupplier = () ->
                DispatchingStateBuilder.dispatching(
                    stateClassInfo.getInitialEventDispatcher(),
                    stateClassInfo.getUpdateEventDispatcher());

        return EventSourcingStateRepository.using(
                aggregateStateBuilderSupplier,
                eventSource,
                stateClassInfo.getEventTypeBinding(),
                stateClassInfo.getCausalOrder());
    }

}
