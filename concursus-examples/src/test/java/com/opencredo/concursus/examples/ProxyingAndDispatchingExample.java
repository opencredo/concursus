package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.events.channels.EventOutChannel;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.dispatching.DispatchingEventOutChannel;
import com.opencredo.concursus.mapping.events.methods.proxying.EventEmittingProxy;
import org.junit.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ProxyingAndDispatchingExample {

    @Test
    public void proxyAndDispatch() {
        // Create a mock handler for person events, and an outChannel that sends events to the handler.
        Person.Events handler = mock(Person.Events.class);
        EventOutChannel outChannel = DispatchingEventOutChannel.toHandler(Person.Events.class, handler);

        // Create a proxy that sends events to the outChannel.
        Person.Events proxy = EventEmittingProxy.proxying(outChannel, Person.Events.class);

        // Send an event via the proxy.
        proxy.created(StreamTimestamp.now(), UUID.randomUUID(), "Arthur Putey", LocalDate.parse("1968-05-28"));

        // Verify that the handler received the event.
        verify(handler).created(any(StreamTimestamp.class), any(UUID.class), eq("Arthur Putey"), eq(LocalDate.parse("1968-05-28")));
    }
}
