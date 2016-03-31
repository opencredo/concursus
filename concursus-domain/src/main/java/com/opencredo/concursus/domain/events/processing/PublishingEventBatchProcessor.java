package com.opencredo.concursus.domain.events.processing;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.domain.events.publishing.EventPublisher;

import java.util.Collection;

/**
 * On {@link com.opencredo.concursus.domain.events.batching.EventBatch} completion, writes the {@link Event}s in the
 * batch to an {@link EventLog}, then publishes the logged events to an {@link EventPublisher}.
 */
public final class PublishingEventBatchProcessor implements EventBatchProcessor {

    /**
     * Create an {@link EventBatchProcessor} that writes the {@link Event}s in an
     * {@link com.opencredo.concursus.domain.events.batching.EventBatch} to an {@link EventLog}, then publishes the
     * logged events to an {@link EventPublisher}.
     * @param eventLog The {@link EventLog} to write the events to.
     * @param eventPublisher The {@link EventPublisher} to publish the events to.
     * @return The constructed {@link EventBatchProcessor}
     */
    public static PublishingEventBatchProcessor using(EventLog eventLog, EventPublisher eventPublisher) {
        return new PublishingEventBatchProcessor(eventLog, eventPublisher);
    }

    private final EventLog eventLog;
    private final EventPublisher eventPublisher;

    private PublishingEventBatchProcessor(EventLog eventLog, EventPublisher eventPublisher) {
        this.eventLog = eventLog;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void accept(Collection<Event> events) {
        eventLog.apply(events).forEach(eventPublisher);
    }
}
