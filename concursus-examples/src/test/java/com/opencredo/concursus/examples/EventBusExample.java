package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventOutChannel;
import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.filtering.batch.EventBatchFilter;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.dispatching.DispatchingEventOutChannel;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

public class EventBusExample {

    @Test
    public void batchEventsViaEventBus() {
        // A filter that enables us to observe batches as they are completed.
        List<Collection<Event>> observedBatches = new ArrayList<>();
        EventBatchFilter batchFilter = batch -> batch.bufferingTo(observedBatches::add);

        // A mock handler, and an EventOutChannel that dispatches events to that handler.
        Person.Events handler = mock(Person.Events.class);
        EventOutChannel outChannel = DispatchingEventOutChannel.toHandler(Person.Events.class, handler);

        // An EventBatchProcessor that forwards all the events in the batch to the outChannel
        EventBatchProcessor batchProcessor = EventBatchProcessor.forwardingTo(outChannel.toEventsOutChannel());

        // A ProxyingEventBus that proxies an EventBus which filters EventBatches with our filter, and processes
        // them with our processor.
        ProxyingEventBus eventBus = ProxyingEventBus.proxying(EventBus.processingWith(batchProcessor, batchFilter));

        StreamTimestamp timestamp = StreamTimestamp.now();
        String personId = "id1";

        // Send three events, in a single batch, to the EventBus.
        eventBus.dispatch(Person.Events.class, e -> {
            e.created(timestamp, personId, "Arthur Putey", LocalDate.parse("1968-05-28"));
            e.changedName(timestamp.plus(1, MINUTES), personId, "Arthur Mumby");
            e.deleted(timestamp.plus(2, MINUTES), personId);
        });

        // Verify that all events were sent to the handler.
        verify(handler).created(timestamp, personId, "Arthur Putey", LocalDate.parse("1968-05-28"));
        verify(handler).changedName(timestamp.plus(1, MINUTES), personId, "Arthur Mumby");
        verify(handler).deleted(timestamp.plus(2, MINUTES), personId);
        verifyNoMoreInteractions(handler);

        // Assert that observedBatches contains a batch with three events in it.
        assertThat(observedBatches, hasItem(hasSize(3)));
    }
}
