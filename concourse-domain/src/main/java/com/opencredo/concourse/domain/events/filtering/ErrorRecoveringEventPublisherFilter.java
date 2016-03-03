package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;

@FunctionalInterface
public interface ErrorRecoveringEventPublisherFilter extends EventPublisherIntercepter {

    @Override
    default void onPublish(EventPublisher eventPublisher, Event event) {
        try {
            eventPublisher.accept(event);
        } catch (Exception e) {
            recover(eventPublisher, event, e);
        }
    }

    void recover(EventPublisher eventPublisher, Event event, Exception e);
}
