package com.opencredo.concursus.domain.events.filtering.publisher;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.publishing.EventPublisher;

public interface EventPublisherIntercepter extends EventPublisherFilter {

    @Override
    default EventPublisher apply(EventPublisher eventPublisher) {
        return event -> onAccept(eventPublisher, event);
    }

    void onAccept(EventPublisher eventPublisher, Event event);

}
