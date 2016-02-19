package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;

public interface EventPublisherIntercepter extends EventPublisherFilter {

    @Override
    default EventPublisher apply(EventPublisher eventPublisher) {
        return event -> onPublish(eventPublisher, event);
    }

    void onPublish(EventPublisher eventPublisher, Event event);

}
