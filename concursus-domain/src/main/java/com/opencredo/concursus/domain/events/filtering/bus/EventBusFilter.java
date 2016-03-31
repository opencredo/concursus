package com.opencredo.concursus.domain.events.filtering.bus;

import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.filtering.Filters;

import java.util.Arrays;
import java.util.function.UnaryOperator;

@FunctionalInterface
public interface EventBusFilter extends UnaryOperator<EventBus> {

    static EventBusFilter compose(EventBusFilter...eventBusFilters) {
        return Filters.compose(Arrays.asList(eventBusFilters))::apply;
    }

}
