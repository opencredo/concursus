package com.opencredo.concourse.domain.events.filtering.publisher;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;

public interface EventPublisherPostFilter extends EventPublisherIntercepter {

    @Override
    default void onAccept(EventPublisher eventPublisher, Event event) {
        eventPublisher.accept(event);
        afterAccept(eventPublisher, event);
    }

    void afterAccept(EventPublisher eventPublisher, Event event);
}
