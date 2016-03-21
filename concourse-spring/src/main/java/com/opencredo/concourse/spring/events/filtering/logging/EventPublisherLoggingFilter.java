package com.opencredo.concourse.spring.events.filtering.logging;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.filtering.publisher.EventPublisherPostFilter;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;
import com.opencredo.concourse.spring.events.filtering.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Filter
public final class EventPublisherLoggingFilter implements EventPublisherPostFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherLoggingFilter.class);

    @Override
    public void afterAccept(EventPublisher eventPublisher, Event event) {
        LOGGER.info("Event publisher received event {}", event);
    }
}
