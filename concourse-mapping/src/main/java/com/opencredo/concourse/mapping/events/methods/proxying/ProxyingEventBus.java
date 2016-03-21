package com.opencredo.concourse.mapping.events.methods.proxying;

import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.functional.Consumers;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An {@link EventBus} that generates proxies for event-emitter interfaces.
 */
public interface ProxyingEventBus extends EventBus {

    /**
     * Create a {@link ProxyingEventBus} that dispatches events via the supplied {@link EventBus}
     * @param eventBus The {@link EventBus} to dispatch events to.
     * @return The constructed {@link ProxyingEventBus}.
     */
    static ProxyingEventBus proxying(EventBus eventBus) {
        return eventBus::startBatch;
    }

    /**
     * Create a proxy instance of the supplied interface, and pass it to the supplied {@link Consumer} to emit events.
     * @param klass The interface to proxy.
     * @param dispatcherConsumer The {@link Consumer} that will use the proxy object to emit events.
     * @param <T> The type of the proxy object.
     */
    default <T> void dispatch(Class<T> klass, Consumer<T> dispatcherConsumer) {
        dispatch(Consumers.transform(
                dispatcherConsumer,
                eventConsumer -> EventEmittingProxy.proxying(eventConsumer, klass)));
    }


    /**
     * Create proxy instances of the supplied interfaces, and pass them to the supplied {@link BiConsumer} to emit events.
     * @param leftKlass The "left" interface to proxy.
     * @param rightKlass The "right" interface to proxy.
     * @param dispatchersConsumer The {@link BiConsumer} that will use the proxy objects to emit events.
     * @param <L> The type of the "left" proxy object.
     * @param <R> The type of the "right" proxy object.
     */
    default <L, R> void dispatch(Class<L> leftKlass, Class<R> rightKlass, BiConsumer<L, R> dispatchersConsumer) {
        dispatch(eventConsumer -> dispatchersConsumer.accept(
                EventEmittingProxy.proxying(eventConsumer, leftKlass),
                EventEmittingProxy.proxying(eventConsumer, rightKlass)
        ));
    }

    /**
     * Get a proxy instance of the supplied interface that will create and complete a distinct batch containing a single
     * event for each event-emitting method invocation.
     * @param klass The interface to proxy.
     * @param <T> The type of the proxy object.
     * @return The constructed proxy object.
     */
    default <T> T getDispatcherFor(Class<T> klass) {
        return EventEmittingProxy.proxying(this, klass);
    }

}
