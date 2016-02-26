package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.EventReplayer;
import com.opencredo.concourse.mapping.events.methods.reflection.MultiEventDispatcher;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class DispatchingEventReplayer<T> {

    static <T> DispatchingEventReplayer<T> dispatching(Comparator<Event> causalOrderComparator, MultiEventDispatcher<T> mapper, EventReplayer eventReplayer) {
        return new DispatchingEventReplayer<>(causalOrderComparator, mapper, eventReplayer);
    }

    private final Comparator<Event> causalOrderComparator;
    private final MultiEventDispatcher<T> dispatcher;
    private final EventReplayer eventReplayer;

    private DispatchingEventReplayer(Comparator<Event> causalOrderComparator, MultiEventDispatcher<T> dispatcher, EventReplayer eventReplayer) {
        this.causalOrderComparator = causalOrderComparator;
        this.dispatcher = dispatcher;
        this.eventReplayer = eventReplayer;
    }

    public DispatchingEventReplayer<T> inDescendingOrder() {
        return new DispatchingEventReplayer<>(causalOrderComparator, dispatcher, eventReplayer.inDescendingOrder());
    }

    public DispatchingEventReplayer<T> inDescendingOrder(Comparator<Event> comparator) {
        return new DispatchingEventReplayer<>(causalOrderComparator, dispatcher, eventReplayer.inDescendingOrder(comparator));
    }

    public DispatchingEventReplayer<T> inDescendingCausalOrder() {
        return inDescendingOrder(causalOrderComparator);
    }

    public DispatchingEventReplayer<T> inAscendingOrder() {
        return new DispatchingEventReplayer<>(causalOrderComparator, dispatcher, eventReplayer.inAscendingOrder());
    }

    public DispatchingEventReplayer<T> inAscendingOrder(Comparator<Event> comparator) {
        return new DispatchingEventReplayer<>(causalOrderComparator, dispatcher, eventReplayer.inAscendingOrder(comparator));
    }

    public DispatchingEventReplayer<T> inAscendingCausalOrder() {
        return inAscendingOrder(causalOrderComparator);
    }

    public DispatchingEventReplayer<T> filter(Predicate<Event> predicate) {
        return new DispatchingEventReplayer<>(causalOrderComparator, dispatcher, eventReplayer.filter(predicate));
    }

    public void replayFirst(T handler) {
        eventReplayer.replayFirst(BoundEventDispatcher.binding(dispatcher, handler));
    }

    public void replayAll(T handler) {
        eventReplayer.replayAll(BoundEventDispatcher.binding(dispatcher, handler));
    }

    public <V> Optional<V> collectFirst(Function<Consumer<V>, T> handlerBuilder) {
        return eventReplayer.collectFirst(caller -> BoundEventDispatcher.binding(dispatcher, handlerBuilder.apply(caller)));
    }

    public <V> List<V> collectAll(Function<Consumer<V>, T> handlerBuilder) {
        return eventReplayer.collectAll(caller -> BoundEventDispatcher.binding(dispatcher, handlerBuilder.apply(caller)));
    }
}
