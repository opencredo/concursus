package com.opencredo.concourse.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.events.channels.EventOutChannel;
import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.events.processing.EventBatchProcessor;
import com.opencredo.concourse.domain.json.events.channels.JsonEventsInChannel;
import com.opencredo.concourse.domain.json.events.channels.JsonEventsOutChannel;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingEventOutChannel;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concourse.mapping.events.methods.reflection.EmitterInterfaceInfo;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class JsonExample {

    @Test
    public void proxyAndSerialiseAndDeserialiseAndDispatch() {
        // A list to collect serialised event batches into.
        List<String> serialisedBatches = new ArrayList<>();

        // Create a channel that sends events to a mocked handler.
        PersonEvents handler = mock(PersonEvents.class);
        EventOutChannel channelToHandler = DispatchingEventOutChannel.toHandler(PersonEvents.class, handler);

        // Create in and out channels that map event batches into JSON lists.
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        JsonEventsOutChannel jsonOut = JsonEventsOutChannel.using(mapper, serialisedBatches::add);
        JsonEventsInChannel jsonIn = JsonEventsInChannel.using(
                mapper,
                EmitterInterfaceInfo.forInterface(PersonEvents.class).getEventTypeMatcher(),
                channelToHandler.toEventsOutChannel());

        // Create an event bus that sends events to the JSON out channel.
        ProxyingEventBus eventBus = ProxyingEventBus.proxying(
                EventBus.processingWith(
                        EventBatchProcessor.forwardingTo(jsonOut)));

        // Send two batches of events.
        eventBus.dispatch(PersonEvents.class, e -> {
            e.created(StreamTimestamp.now(), UUID.randomUUID(), "Arthur Putey", LocalDate.parse("1968-05-28"));
            e.created(StreamTimestamp.now(), UUID.randomUUID(), "Arthur Mumby", LocalDate.parse("1954-02-17"));
        });

        eventBus.dispatch(PersonEvents.class, e -> {
            e.created(StreamTimestamp.now(), UUID.randomUUID(), "Arthur Daley", LocalDate.parse("1962-08-12"));
        });

        // Send each serialised batch to the JSON in channel.
        serialisedBatches.forEach(jsonIn);

        // Verify that the batches are deserialised and events dispatched to the handlers.
        verify(handler).created(any(StreamTimestamp.class), any(UUID.class), eq("Arthur Putey"), eq(LocalDate.parse("1968-05-28")));
        verify(handler).created(any(StreamTimestamp.class), any(UUID.class), eq("Arthur Mumby"), eq(LocalDate.parse("1954-02-17")));
        verify(handler).created(any(StreamTimestamp.class), any(UUID.class), eq("Arthur Daley"), eq(LocalDate.parse("1962-08-12")));
        verifyNoMoreInteractions(handler);

        // Verify that two batches were recorded.
        assertThat(serialisedBatches, hasSize(2));
    }
}
