package com.opencredo.concursus.domain.events.filtering.publisher;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.publishing.EventPublisher;

public interface EventPublisherPreFilter extends EventPublisherIntercepter {

    @Override
    default void onAccept(EventPublisher eventPublisher, Event event) {
        if (beforeAccept(eventPublisher, event)) {
            eventPublisher.accept(event);
        }
    }

    boolean beforeAccept(EventPublisher eventPublisher, Event event);
}
