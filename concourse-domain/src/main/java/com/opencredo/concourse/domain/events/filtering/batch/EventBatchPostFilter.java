package com.opencredo.concourse.domain.events.filtering.batch;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.batching.EventBatch;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface EventBatchPostFilter extends EventBatchIntercepter {

    static EventBatchPostFilter of(Consumer<EventBatch> afterComplete, BiConsumer<EventBatch, Event> afterAccept) {
        return new EventBatchPostFilter() {
            @Override
            public void afterComplete(EventBatch eventBatch) {
                afterComplete.accept(eventBatch);
            }

            @Override
            public void afterAccept(EventBatch eventBatch, Event event) {
                afterAccept.accept(eventBatch, event);
            }
        };
    }

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
