package com.opencredo.concursus.domain.events.filtering.publisher;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.publishing.EventPublisher;

public interface EventPublisherPostFilter extends EventPublisherIntercepter {

    @Override
    default void onAccept(EventPublisher eventPublisher, Event event) {
        eventPublisher.accept(event);
        afterAccept(eventPublisher, event);
    }

    void afterAccept(EventPublisher eventPublisher, Event event);
}
