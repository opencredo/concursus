package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;

public interface EventPublisherPreFilter extends EventPublisherIntercepter {

    @Override
    default void onPublish(EventPublisher eventPublisher, Event event) {
        if (beforePublish(eventPublisher, event)) {
            eventPublisher.accept(event);
        }
    }

    boolean beforePublish(EventPublisher eventPublisher, Event event);
}
