package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.domain.events.EventBus;
import com.opencredo.concourse.domain.functional.Consumers;

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

    default <T> T getDispatcherFor(Class<T> klass) {
        return EventEmittingProxy.proxying(this, klass);
    }

}
