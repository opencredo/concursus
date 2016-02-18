package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.dispatching.EventBus;

import java.util.Arrays;
import java.util.function.UnaryOperator;

public interface EventBusFilter extends UnaryOperator<EventBus> {

    static EventBusFilter compose(EventBusFilter...eventBusFilters) {
        return Filters.compose(Arrays.asList(eventBusFilters))::apply;
    }

}
