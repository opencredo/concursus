package com.opencredo.concursus.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.events.channels.EventOutChannel;
import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.json.events.channels.JsonEventsInChannel;
import com.opencredo.concursus.domain.json.events.channels.JsonEventsOutChannel;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.dispatching.DispatchingEventOutChannel;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concursus.mapping.events.methods.reflection.EmitterInterfaceInfo;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class JsonExample {

    @Test
    public void proxyAndSerialiseAndDeserialiseAndDispatch() {
        // A list to collect serialised event batches into.
        List<String> serialisedBatches = new ArrayList<>();

        // Create a channel that sends events to a mocked handler.
        Person.Events handler = mock(Person.Events.class);
        EventOutChannel channelToHandler = DispatchingEventOutChannel.toHandler(Person.Events.class, handler);

        // Create in and out channels that map event batches into JSON lists.
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        JsonEventsOutChannel jsonOut = JsonEventsOutChannel.using(mapper, e -> serialisedBatches.add(e));
        JsonEventsInChannel jsonIn = JsonEventsInChannel.using(
                mapper,
                EmitterInterfaceInfo.forInterface(Person.Events.class).getEventTypeMatcher(),
                channelToHandler.toEventsOutChannel());

        // Create an event bus that sends events to the JSON out channel.
        ProxyingEventBus eventBus = ProxyingEventBus.proxying(
                EventBus.processingWith(
                        EventBatchProcessor.forwardingTo(jsonOut)));

        // Send two batches of events.
        eventBus.dispatch(Person.Events.class, e -> {
            e.created(StreamTimestamp.now(), "id1", "Arthur Putey", LocalDate.parse("1968-05-28"));
            e.created(StreamTimestamp.now(), "id2", "Arthur Mumby", LocalDate.parse("1954-02-17"));
        });

        eventBus.dispatch(Person.Events.class, e -> {
            e.created(StreamTimestamp.now(), "id3", "Arthur Daley", LocalDate.parse("1962-08-12"));
        });

        // Send each serialised batch to the JSON in channel.
        serialisedBatches.forEach(jsonIn);

        // Verify that the batches are deserialised and events dispatched to the handlers.
        verify(handler).created(any(StreamTimestamp.class), eq("id1"), eq("Arthur Putey"), eq(LocalDate.parse("1968-05-28")));
        verify(handler).created(any(StreamTimestamp.class), eq("id2"), eq("Arthur Mumby"), eq(LocalDate.parse("1954-02-17")));
        verify(handler).created(any(StreamTimestamp.class), eq("id2"), eq("Arthur Daley"), eq(LocalDate.parse("1962-08-12")));
        verifyNoMoreInteractions(handler);

        // Verify that two batches were recorded.
        assertThat(serialisedBatches, hasSize(2));
    }
}
