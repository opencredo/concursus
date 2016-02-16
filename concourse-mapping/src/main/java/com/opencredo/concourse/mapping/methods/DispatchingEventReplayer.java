package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.EventReplayer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class DispatchingEventReplayer<T> {

    public static <T> DispatchingEventReplayer<T> dispatching(Class<T> handlerClass, EventReplayer eventReplayer) {
        return new DispatchingEventReplayer<>(handlerClass, eventReplayer);
    }

    private final Class<T> handlerClass;
    private final EventReplayer eventReplayer;

    private DispatchingEventReplayer(Class<T> handlerClass, EventReplayer eventReplayer) {
        this.handlerClass = handlerClass;
        this.eventReplayer = eventReplayer;
    }

    public DispatchingEventReplayer<T> inOrder(Comparator<Event> comparator) {
        return new DispatchingEventReplayer<>(handlerClass, eventReplayer.inOrder(comparator));
    }

    public DispatchingEventReplayer<T> inReverseOrder() {
        return new DispatchingEventReplayer<>(handlerClass, eventReplayer.inReverseOrder());
    }

    public DispatchingEventReplayer<T> inReverseOrder(Comparator<Event> comparator) {
        return new DispatchingEventReplayer<>(handlerClass, eventReplayer.inReverseOrder(comparator));
    }

    public DispatchingEventReplayer<T> filter(Predicate<Event> predicate) {
        return new DispatchingEventReplayer<>(handlerClass, eventReplayer.filter(predicate));
    }

    public void replayFirst(T handler) {
        eventReplayer.replayFirst(EventMethodDispatcher.toHandler(handlerClass, handler));
    }

    public void replayAll(T handler) {
        eventReplayer.replayAll(EventMethodDispatcher.toHandler(handlerClass, handler));
    }

    public <V> Optional<V> collectFirst(Function<Consumer<V>, T> handlerBuilder) {
        return eventReplayer.collectFirst(EventMethodDispatcher.toCollector(handlerClass, handlerBuilder));
    }

    public <V> List<V> collectAll(Function<Consumer<V>, T> handlerBuilder) {
        return eventReplayer.collectAll(EventMethodDispatcher.toCollector(handlerClass, handlerBuilder));
    }
}
