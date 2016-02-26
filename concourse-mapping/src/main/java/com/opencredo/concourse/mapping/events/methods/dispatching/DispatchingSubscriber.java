package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.events.publishing.EventSubscribable;
import com.opencredo.concourse.mapping.events.methods.reflection.EventInterfaceInfo;

public final class DispatchingSubscriber {

    public static DispatchingSubscriber subscribingTo(EventSubscribable eventPublisher) {
        return new DispatchingSubscriber(eventPublisher);
    }

    private final EventSubscribable eventPublisher;

    public DispatchingSubscriber(EventSubscribable eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public <H> DispatchingSubscriber subscribe(Class<? extends H> handlerClass, H handler) {
        return subscribe(EventInterfaceInfo.forInterface(handlerClass), handler);
    }

    public <H> DispatchingSubscriber subscribe(EventInterfaceInfo<H> mapper, H handler) {
        BoundEventDispatcher<H> dispatcher = BoundEventDispatcher.binding(mapper.getEventDispatcher(), handler);
        dispatcher.subscribeTo(eventPublisher);
        return this;
    }
}
