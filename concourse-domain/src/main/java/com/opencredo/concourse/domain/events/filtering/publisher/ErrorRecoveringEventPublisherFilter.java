package com.opencredo.concourse.domain.events.filtering.publisher;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;

@FunctionalInterface
public interface ErrorRecoveringEventPublisherFilter extends EventPublisherIntercepter {

    @Override
    default void onAccept(EventPublisher eventPublisher, Event event) {
        try {
            eventPublisher.accept(event);
        } catch (Exception e) {
            recover(eventPublisher, event, e);
        }
    }

    void recover(EventPublisher eventPublisher, Event event, Exception e);
}
