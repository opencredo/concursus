package com.opencredo.concourse.domain.events.batching;

import com.opencredo.concourse.domain.events.Event;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * A collection of {@link Event}s that are to be processed together.
 */
public interface EventBatch extends Consumer<Event> {

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

}
