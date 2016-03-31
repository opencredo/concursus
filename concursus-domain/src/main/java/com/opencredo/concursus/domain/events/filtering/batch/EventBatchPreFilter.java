package com.opencredo.concursus.domain.events.filtering.batch;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.batching.EventBatch;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface EventBatchPreFilter extends EventBatchIntercepter {

    static EventBatchPreFilter of(Function<EventBatch, Boolean> beforeComplete, BiFunction<EventBatch, Event, Boolean> beforeAccept) {
        return new EventBatchPreFilter() {
            @Override
            public boolean beforeComplete(EventBatch eventBatch) {
                return beforeComplete.apply(eventBatch);
            }

            @Override
            public boolean beforeAccept(EventBatch eventBatch, Event event) {
                return beforeAccept.apply(eventBatch, event);
            }
        };
    }

    @Override
    default void onComplete(EventBatch eventBatch) {
        if (beforeComplete(eventBatch)) {
            eventBatch.complete();
        }
    }

    @Override
    default void onAccept(EventBatch eventBatch, Event event) {
        if (beforeAccept(eventBatch, event)) {
            eventBatch.accept(event);
        }
    }

    boolean beforeComplete(EventBatch eventBatch);
    boolean beforeAccept(EventBatch eventBatch, Event event);

}
