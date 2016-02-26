package com.opencredo.concourse.mapping.events.methods.state;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.domain.events.sourcing.EventReplayer;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

final class StateMethodDispatcher<T> implements Consumer<Event>, Function<EventReplayer, Optional<T>> {

    private Optional<T> state = Optional.empty();

    private final Map<EventType, StateFactoryMethodDispatcher<T>> factoryMethodDispatchers;
    private final Map<EventType, StateUpdateMethodDispatcher> updateMethodDispatchers;
    private final Comparator<Event> causalOrder;

    StateMethodDispatcher(Map<EventType, StateFactoryMethodDispatcher<T>> factoryMethodDispatchers, Map<EventType, StateUpdateMethodDispatcher> updateMethodDispatchers, Comparator<Event> causalOrder) {
        this.factoryMethodDispatchers = factoryMethodDispatchers;
        this.updateMethodDispatchers = updateMethodDispatchers;
        this.causalOrder = causalOrder;
    }

    @Override
    public void accept(Event event) {
        checkNotNull(event, "event must not be null");

        if (!state.isPresent()) {
            createState(event);
        } else {
            state.ifPresent(s -> updateState(s, event));
        }
    }

    private void createState(Event event) {
        Function<Event, T> createEventMapper = factoryMethodDispatchers.get(EventType.of(event));
        checkState(createEventMapper != null,
                "No create event mapper found for event %s", event);
        state = Optional.of(createEventMapper.apply(event));
    }

    private void updateState(T state, Event event) {
        BiConsumer<Object, Event> eventMapper = updateMethodDispatchers.get(EventType.of(event));
        checkState(eventMapper != null,
                "No method dispatcher found for event %s", event);

        eventMapper.accept(state, event);
    }

    @Override
    public Optional<T> apply(EventReplayer eventReplayer) {
        state = Optional.empty();
        eventReplayer.inAscendingOrder(causalOrder).replayAll(this);
        return state;
    }
}
