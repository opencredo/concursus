package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.batching.EventBatch;

import java.util.UUID;

public interface ObservingEventBatchFilter extends EventBatchFilter {

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
