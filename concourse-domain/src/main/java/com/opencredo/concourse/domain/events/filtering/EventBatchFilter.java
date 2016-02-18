package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.batching.EventBatch;

import java.util.Arrays;
import java.util.function.UnaryOperator;

@FunctionalInterface
public interface EventBatchFilter extends UnaryOperator<EventBatch> {

    static EventBatchFilter compose(EventBatchFilter...filters) {
        return Filters.compose(Arrays.asList(filters))::apply;
    }

}
