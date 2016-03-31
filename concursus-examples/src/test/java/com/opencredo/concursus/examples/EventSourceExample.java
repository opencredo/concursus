package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.events.sourcing.EventSource;
import com.opencredo.concursus.domain.storing.InMemoryEventStore;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.dispatching.DispatchingEventSourceFactory;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.MINUTES;

public class EventSourceExample {

    private final InMemoryEventStore eventStore = InMemoryEventStore.empty();
    private final EventBus eventBus = EventBus.processingWith(EventBatchProcessor.forwardingTo(eventStore));
    private final EventSource eventSource = EventSource.retrievingWith(eventStore);

    private final ProxyingEventBus proxyingEventBus = ProxyingEventBus.proxying(eventBus);
    private final DispatchingEventSourceFactory eventSourceFactory = DispatchingEventSourceFactory.dispatching(eventSource);

    @Test
    public void replayEventsToHandler() {
        StreamTimestamp start = StreamTimestamp.now();
        UUID personId = UUID.randomUUID();

        proxyingEventBus.dispatch(Person.Events.class, personEvents -> {
            personEvents.created(start.plus(3, MINUTES), personId, "Ludwig Wittgenstein", LocalDate.parse("1968-05-28"));
            personEvents.changedName(start.plus(2, MINUTES), personId, "Gilbert Ryle");
            personEvents.changedName(start.plus(1, MINUTES), personId, "Wilfrid Sellars");
        });

        Person.Events handler = Mockito.mock(Person.Events.class);
        eventSourceFactory.dispatchingTo(Person.Events.class)
                .replaying(personId)
                .inAscendingCausalOrder()
                .replayAll(handler);

        InOrder inOrder = Mockito.inOrder(handler);
        inOrder.verify(handler).created(start.plus(3, MINUTES), personId, "Ludwig Wittgenstein", LocalDate.parse("1968-05-28"));
        inOrder.verify(handler).changedName(start.plus(1, MINUTES), personId, "Wilfrid Sellars");
        inOrder.verify(handler).changedName(start.plus(2, MINUTES), personId, "Gilbert Ryle");
        inOrder.verifyNoMoreInteractions();
    }

}
