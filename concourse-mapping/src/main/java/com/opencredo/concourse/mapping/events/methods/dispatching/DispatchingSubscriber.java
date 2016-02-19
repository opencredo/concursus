package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.events.publishing.EventSubscribable;

public final class DispatchingSubscriber {

    public static DispatchingSubscriber subscribingTo(EventSubscribable eventPublisher) {
        return new DispatchingSubscriber(eventPublisher);
    }

    private final EventSubscribable eventPublisher;

    public DispatchingSubscriber(EventSubscribable eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public <H> DispatchingSubscriber subscribe(Class<? extends H> handlerClass, H handler) {
        EventMethodDispatcher dispatcher = EventMethodDispatcher.toHandler(handlerClass, handler);
        dispatcher.subscribeTo(eventPublisher);
        return this;
    }
}
