package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.publishing.EventPublisher;

import java.util.Arrays;
import java.util.function.UnaryOperator;

@FunctionalInterface
public interface EventPublisherFilter extends UnaryOperator<EventPublisher> {

    static EventPublisherFilter compose(EventPublisherFilter...filters) {
        return Filters.compose(Arrays.asList(filters))::apply;
    }

}
