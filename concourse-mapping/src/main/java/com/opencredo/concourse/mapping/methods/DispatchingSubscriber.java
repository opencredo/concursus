package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.domain.events.publishing.Subscribable;

public final class DispatchingSubscriber {

    public static DispatchingSubscriber subscribingTo(Subscribable eventPublisher) {
        return new DispatchingSubscriber(eventPublisher);
    }

    private final Subscribable eventPublisher;

    public DispatchingSubscriber(Subscribable eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public <H> DispatchingSubscriber subscribe(Class<? extends H> handlerClass, H handler) {
        EventMethodDispatcher dispatcher = EventMethodDispatcher.toHandler(handlerClass, handler);
        dispatcher.subscribeTo(eventPublisher);
        return this;
    }
}
