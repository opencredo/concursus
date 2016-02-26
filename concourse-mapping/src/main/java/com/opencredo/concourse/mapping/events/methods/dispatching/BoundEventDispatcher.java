package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.publishing.EventSubscribable;
import com.opencredo.concourse.mapping.events.methods.reflection.MultiEventDispatcher;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

final class BoundEventDispatcher<H> implements Consumer<Event> {

    static <H> BoundEventDispatcher<H> binding(MultiEventDispatcher<H> dispatcher, H target) {
        checkNotNull(dispatcher, "dispatcher must not be null");
        checkNotNull(target, "target must not be null");

        return new BoundEventDispatcher<>(target, dispatcher);
    }

    private final H target;
    private final MultiEventDispatcher<H> eventDispatcher;

    private BoundEventDispatcher(H target, MultiEventDispatcher<H> eventDispatcher) {
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
