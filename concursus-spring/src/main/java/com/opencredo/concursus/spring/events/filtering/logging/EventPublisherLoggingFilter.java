package com.opencredo.concursus.spring.events.filtering.logging;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.filtering.publisher.EventPublisherPostFilter;
import com.opencredo.concursus.domain.events.publishing.EventPublisher;
import com.opencredo.concursus.spring.events.filtering.Filter;
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
