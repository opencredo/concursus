package com.opencredo.concourse.domain.events.filtering.publisher;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;

public interface EventPublisherIntercepter extends EventPublisherFilter {

    @Override
    default EventPublisher apply(EventPublisher eventPublisher) {
        return event -> onAccept(eventPublisher, event);
    }

    void onAccept(EventPublisher eventPublisher, Event event);

}
