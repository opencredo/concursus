package com.opencredo.concourse.domain.events.filtering.batch;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.batching.EventBatch;

public interface EventBatchPostFilter extends EventBatchIntercepter {

    @Override
    default void onComplete(EventBatch eventBatch) {
        eventBatch.complete();
        afterComplete(eventBatch);
    }

    @Override
    default void onAccept(EventBatch eventBatch, Event event) {
        eventBatch.accept(event);
        afterAccept(eventBatch, event);
    }

    void afterComplete(EventBatch eventBatch);
    void afterAccept(EventBatch eventBatch, Event event);

}
