package com.opencredo.concourse.mapping.events.methods.proxying;

import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.functional.Consumers;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface ProxyingEventBus extends EventBus {

    static ProxyingEventBus proxying(EventBus eventBus) {
        return eventBus::startBatch;
    }

    default <T> void dispatch(Class<T> klass, Consumer<T> dispatcherConsumer) {
        dispatch(Consumers.transform(
                dispatcherConsumer,
                eventConsumer -> EventEmittingProxy.proxying(eventConsumer, klass)));
    }

    default <L, R> void dispatch(Class<L> leftKlass, Class<R> rightKlass, BiConsumer<L, R> dispatchersConsumer) {
        dispatch(eventConsumer -> dispatchersConsumer.accept(
                EventEmittingProxy.proxying(eventConsumer, leftKlass),
                EventEmittingProxy.proxying(eventConsumer, rightKlass)
        ));
    }

    default <T> T getDispatcherFor(Class<T> klass) {
        return EventEmittingProxy.proxying(this, klass);
    }

}
