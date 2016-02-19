package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;

public interface EventPublisherPostFilter extends EventPublisherIntercepter {

    @Override
    default void onPublish(EventPublisher eventPublisher, Event event) {
        eventPublisher.accept(event);
        afterPublish(eventPublisher, event);
    }

    void afterPublish(EventPublisher eventPublisher, Event event);
}
