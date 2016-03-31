package com.opencredo.concursus.domain.events.filtering.batch;

import com.opencredo.concursus.domain.events.batching.EventBatch;
import com.opencredo.concursus.domain.events.filtering.Filters;

import java.util.Arrays;
import java.util.function.UnaryOperator;

@FunctionalInterface
public interface EventBatchFilter extends UnaryOperator<EventBatch> {

    static EventBatchFilter compose(EventBatchFilter...filters) {
        return Filters.compose(Arrays.asList(filters))::apply;
    }

}
