package com.opencredo.concourse.domain.events.filtering.batch;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.batching.EventBatch;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface EventBatchIntercepter extends EventBatchFilter {

    static EventBatchIntercepter of(Consumer<EventBatch> onCompleted, BiConsumer<EventBatch, Event> onAccept) {
        return new EventBatchIntercepter() {
            @Override
            public void onComplete(EventBatch eventBatch) {
                onCompleted.accept(eventBatch);
            }

            @Override
            public void onAccept(EventBatch eventBatch, Event event) {
                onAccept.accept(eventBatch, event);
            }
        };
    }

    default EventBatch apply(EventBatch eventBatch) {
        return new EventBatch() {
            @Override
            public UUID getId() {
                return eventBatch.getId();
            }

            @Override
            public void complete() {
                onComplete(eventBatch);
            }

            @Override
            public void accept(Event event) {
                onAccept(eventBatch, event);
            }
        };
    }

    void onComplete(EventBatch eventBatch);
    void onAccept(EventBatch eventBatch, Event event);

}
