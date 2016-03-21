package com.opencredo.concourse.domain.events.batching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.processing.EventBatchProcessor;
import com.opencredo.concourse.domain.time.TimeUUID;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * An {@link EventBatch} that sends the batched {@link Event}s to an {@link EventBatchProcessor} on completion.
 */
public final class ProcessingEventBatch implements EventBatch {

    /**
     * Create an {@link EventBatch} that sends the batched {@link Event}s to the supplied {@link EventBatchProcessor} on completion.
     * @param batchProcessor The {@link EventBatchProcessor} to process events with.
     * @return The constructed {@link EventBatch}.
     */
    public static EventBatch processingWith(EventBatchProcessor batchProcessor) {
        return new ProcessingEventBatch(batchProcessor);
    }

    private final UUID id = TimeUUID.timeBased();
    private final List<Event> events = new LinkedList<>();
    private final EventBatchProcessor batchProcessor;

    private ProcessingEventBatch(EventBatchProcessor batchProcessor) {
        this.batchProcessor = batchProcessor;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void complete() {
        batchProcessor.accept(events);
    }

    @Override
    public void accept(Event event) {
        events.add(event);
    }
}
