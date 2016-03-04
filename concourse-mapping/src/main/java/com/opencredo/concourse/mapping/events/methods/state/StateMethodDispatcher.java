package com.opencredo.concourse.mapping.events.methods.state;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.EventReplayer;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.EventDispatcher;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.InitialEventDispatcher;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.opencredo.concourse.domain.events.EventCharacteristics.IS_INITIAL;

final class StateMethodDispatcher<T> implements Consumer<Event>, Function<EventReplayer, Optional<T>> {

    public static <T> StateMethodDispatcher<T> dispatching(InitialEventDispatcher<T> initialEventDispatcher, EventDispatcher<T> updateMethodDispatcher, Comparator<Event> causalOrder) {
        return new StateMethodDispatcher<>(initialEventDispatcher, updateMethodDispatcher, causalOrder);
    }

    private Optional<T> state = Optional.empty();

    private final InitialEventDispatcher<T> initialEventDispatcher;
    private final EventDispatcher<T> updateMethodDispatcher;
    private final Comparator<Event> causalOrder;

    private StateMethodDispatcher(InitialEventDispatcher<T> initialEventDispatcher, EventDispatcher<T> updateMethodDispatcher, Comparator<Event> causalOrder) {
        this.initialEventDispatcher = initialEventDispatcher;
        this.updateMethodDispatcher = updateMethodDispatcher;
        this.causalOrder = causalOrder;
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
    public Optional<T> apply(EventReplayer eventReplayer) {
        state = Optional.empty();
        eventReplayer.replayAll(System.out::println);
        eventReplayer.inAscendingOrder(causalOrder).replayAll(this);
        return state;
    }
}
