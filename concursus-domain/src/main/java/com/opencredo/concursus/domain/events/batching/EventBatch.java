package com.opencredo.concursus.domain.events.batching;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventsOutChannel;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * A collection of {@link Event}s that are to be processed together.
 */
public interface EventBatch extends Consumer<Event> {

    static EventBatch processingWith(EventBatchProcessor processor) {
        return ProcessingEventBatch.processingWith(processor);
    }

    /**
     * Add the {@link Event} to the batch.
     * @param event The {@link Event} to add to the batch.
     */
    @Override
    void accept(Event event);

    /**
     * Get the unique {@link UUID} of the batch.
     * @return The unique identifier of the batch.
     */
    UUID getId();

    /**
     * Complete the batch and submit it for processing.
     */
    void complete();

    /**
     * Buffer this event batch and replay buffered events to the supplied {@link EventsOutChannel} on completion.
     * @param outChannel The {@link EventsOutChannel} to replay events to on completion.
     * @return The buffered event batch.
     */
    default EventBatch bufferingTo(EventsOutChannel outChannel) {
        return BufferingEventBatch.buffering(this, outChannel);
    }

}
