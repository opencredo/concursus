package com.opencredo.concourse.examples;

import com.opencredo.concourse.domain.events.channels.EventOutChannel;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingEventOutChannel;
import com.opencredo.concourse.mapping.events.methods.proxying.EventEmittingProxy;
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
        PersonEvents handler = mock(PersonEvents.class);
        EventOutChannel outChannel = DispatchingEventOutChannel.toHandler(PersonEvents.class, handler);

        // Create a proxy that sends events to the outChannel.
        PersonEvents proxy = EventEmittingProxy.proxying(outChannel, PersonEvents.class);

        // Send an event via the proxy.
        proxy.created(StreamTimestamp.now(), UUID.randomUUID(), "Arthur Putey", LocalDate.parse("1968-05-28"));

        // Verify that the handler received the event.
        verify(handler).created(any(StreamTimestamp.class), any(UUID.class), eq("Arthur Putey"), eq(LocalDate.parse("1968-05-28")));
    }
}
