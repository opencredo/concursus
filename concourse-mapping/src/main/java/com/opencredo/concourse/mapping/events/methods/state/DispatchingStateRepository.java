package com.opencredo.concourse.mapping.events.methods.state;

import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.state.StateBuilder;
import com.opencredo.concourse.domain.state.EventSourcingStateRepository;
import com.opencredo.concourse.domain.state.StateRepository;
import com.opencredo.concourse.mapping.events.methods.reflection.StateClassInfo;

import java.util.function.Supplier;

public final class DispatchingStateRepository {

    private DispatchingStateRepository() {
    }

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
