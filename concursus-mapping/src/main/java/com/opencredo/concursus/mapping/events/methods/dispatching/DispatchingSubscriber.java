package com.opencredo.concursus.mapping.events.methods.dispatching;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.publishing.EventSubscribable;
import com.opencredo.concursus.mapping.events.methods.reflection.EmitterInterfaceInfo;

/**
 * Subscribes event handler instances to an event publisher.
 */
public final class DispatchingSubscriber {

    /**
     * Create a {@link DispatchingSubscriber} that subscribes handler instances to the supplied
     * {@link EventSubscribable} event publisher.
     * @param eventPublisher The subscribable event publisher to subscribe handler instances to.
     * @return The constructed {@link DispatchingSubscriber}.
     */
    public static DispatchingSubscriber subscribingTo(EventSubscribable eventPublisher) {
        return new DispatchingSubscriber(eventPublisher);
    }

    private final EventSubscribable eventPublisher;

    private DispatchingSubscriber(EventSubscribable eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Subscribe the supplied handler to handle published {@link Event}s.
     * @param handlerClass The event-emitter interface to map {@link Event}s to.
     * @param handler The handler instance.
     * @param <H> The type of the handler instance.
     * @return This object, for method chaining.
     */
    public <H> DispatchingSubscriber subscribe(Class<? extends H> handlerClass, H handler) {
        return subscribe(EmitterInterfaceInfo.forInterface(handlerClass), handler);
    }

    private <H> DispatchingSubscriber subscribe(EmitterInterfaceInfo<H> mapper, H handler) {
        DispatchingEventOutChannel<H> dispatcher = DispatchingEventOutChannel.binding(mapper.getEventDispatcher(), handler);
        dispatcher.subscribeTo(eventPublisher);
        return this;
    }
}
