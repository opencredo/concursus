package com.opencredo.concourse.domain.events.filtering.publisher;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;

public interface EventPublisherPreFilter extends EventPublisherIntercepter {

    @Override
    default void onAccept(EventPublisher eventPublisher, Event event) {
        if (beforeAccept(eventPublisher, event)) {
            eventPublisher.accept(event);
        }
    }

    boolean beforeAccept(EventPublisher eventPublisher, Event event);
}
