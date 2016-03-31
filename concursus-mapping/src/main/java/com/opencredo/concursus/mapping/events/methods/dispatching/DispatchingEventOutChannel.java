package com.opencredo.concursus.mapping.events.methods.dispatching;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventOutChannel;
import com.opencredo.concursus.domain.events.publishing.EventSubscribable;
import com.opencredo.concursus.mapping.events.methods.reflection.EmitterInterfaceInfo;
import com.opencredo.concursus.mapping.events.methods.reflection.dispatching.MultiTypeEventDispatcher;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DispatchingEventOutChannel<H> implements EventOutChannel {

    public static <T> EventOutChannel toHandler(Class<? extends T> iface, T handler) {
        checkNotNull(iface, "iface must not be null");

        return binding(EmitterInterfaceInfo.forInterface(iface).getEventDispatcher(), handler);
    }

    static <H> DispatchingEventOutChannel<H> binding(MultiTypeEventDispatcher<H> dispatcher, H handler) {
        checkNotNull(handler, "handler must not be null");
        return new DispatchingEventOutChannel<>(handler, dispatcher);
    }

    private final H target;
    private final MultiTypeEventDispatcher<H> eventDispatcher;

    private DispatchingEventOutChannel(H target, MultiTypeEventDispatcher<H> eventDispatcher) {
        this.target = target;
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void accept(Event event) {
        eventDispatcher.accept(target, event);
    }

    public void subscribeTo(EventSubscribable eventPublisher) {
        eventDispatcher.getHandledEventTypes().forEach(eventType -> eventPublisher.subscribe(eventType, this));
    }
}
