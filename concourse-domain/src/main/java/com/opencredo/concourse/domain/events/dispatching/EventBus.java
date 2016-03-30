package com.opencredo.concourse.domain.events.dispatching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.batching.EventBatch;
import com.opencredo.concourse.domain.events.batching.ProcessingEventBatch;
import com.opencredo.concourse.domain.events.channels.EventOutChannel;
import com.opencredo.concourse.domain.events.channels.EventsOutChannel;
import com.opencredo.concourse.domain.events.filtering.batch.BufferingEventBatchFilter;
import com.opencredo.concourse.domain.events.processing.EventBatchProcessor;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Provides {@link EventBatch}es which are used to send groups of {@link Event}s for processing by an
 * {@link com.opencredo.concourse.domain.events.processing.EventBatchProcessor}.
 */
@FunctionalInterface
public interface EventBus extends EventOutChannel {

    static EventBus processingWith(EventBatchProcessor processor) {
        return () -> ProcessingEventBatch.processingWith(processor);
    }

    static EventBus processingWith(EventBatchProcessor processor, UnaryOperator<EventBatch> batchFilter) {
        return () -> batchFilter.apply(ProcessingEventBatch.processingWith(processor));
    }

    /**
     * Starts a new {@link EventBatch}.
     * @return The started {@link EventBatch}.
     */
    EventBatch startBatch();

    /**
     * Create an {@link EventBatch}, add the supplied {@link Event} to it, then complete the batch immediately,
     * sending it for processing.
     * @param event The {@link Event} to process.
     */
    @Override
    default void accept(Event event) {
        dispatch(batch -> batch.accept(event));
    }

    /**
     * Create an {@link EventBatch}, pass it to the supplied batch {@link Consumer} for populating, then complete it.
     * @param batchConsumer A {@link Consumer} that accepts an {@link EventBatch} and adds events to it.
     */
    default void dispatch(Consumer<EventBatch> batchConsumer) {
        EventBatch batch = startBatch();
        batchConsumer.accept(batch);
        batch.complete();
    }

    default EventsOutChannel toEventsOutChannel() {
        return events -> dispatch(batch -> events.forEach(batch::accept));
    }

    /**
     * Create a copy of this event bus that notifies the supplied {@link EventsOutChannel} on batch completion.
     * @param outChannel The {@link EventsOutChannel} to notify.
     * @param busConsumer A {@link Consumer} that will use the subscribed bus.
     */
    default void notifying(EventsOutChannel outChannel, Consumer<EventBus> busConsumer) {
        BufferingEventBatchFilter bufferingEventBatchFilter = BufferingEventBatchFilter.writingOnCompleteTo(outChannel);
        EventBus subscribedBus = () -> bufferingEventBatchFilter.apply(startBatch());
        busConsumer.accept(subscribedBus);
    }
}
