package com.opencredo.concourse.domain.events.filtering.batch;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.batching.EventBatch;

public interface EventBatchPreFilter extends EventBatchIntercepter {

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
