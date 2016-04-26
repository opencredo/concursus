package com.opencredo.concursus.mapping.events.methods.dispatching;

import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.events.processing.PublishingEventBatchProcessor;
import com.opencredo.concursus.domain.events.publishing.SubscribableEventPublisher;
import com.opencredo.concursus.domain.events.storage.InMemoryEventStore;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.annotations.Name;
import com.opencredo.concursus.mapping.events.methods.helper.PersonEvents;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DispatchingSubscriberTest {

    @HandlesEventsFor("person")
    public interface CreatedEventReceiver {

        @Name(value = "created", version = "2")
        void created(StreamTimestamp timestamp, String aggregateId, String name, int age);

    }

    private final InMemoryEventStore eventStore = InMemoryEventStore.empty();
    private final SubscribableEventPublisher publisher = new SubscribableEventPublisher();
    private final EventBatchProcessor batchProcessor = PublishingEventBatchProcessor.using(EventLog.loggingTo(eventStore), publisher);
    private final ProxyingEventBus eventBus = ProxyingEventBus.proxying(EventBus.processingWith(batchProcessor));
    private final DispatchingSubscriber subscriber = DispatchingSubscriber.subscribingTo(publisher);

    private final Instant timestampStart = Instant.now();
    private final AtomicInteger timestampOffset = new AtomicInteger(0);

    private final CreatedEventReceiver createdEventReceiver = mock(CreatedEventReceiver.class);
    private final PersonEvents personEvents = mock(PersonEvents.class);

    @Before
    public void subscribeHandlers() {
        subscriber.subscribe(CreatedEventReceiver.class, createdEventReceiver);
        subscriber.subscribe(PersonEvents.class, personEvents);
    }

    @Test
    public void eventsArePublishedToSubscribedHandlers() {
        String id1 = "id1";
        String id2 = "id2";

        eventBus.dispatch(PersonEvents.class, batch -> {
            batch.createdV2(nextTimestamp(), id1, "Arthur Putey", 41);
            batch.createdV1(nextTimestamp(), id2, "Arthur Dent");
            batch.nameUpdated(nextTimestamp(), id1, "Arthur Daley");
            batch.nameUpdated(nextTimestamp(), id2, "Arthur Mumby");
        });

        verify(createdEventReceiver).created(any(StreamTimestamp.class), eq(id1), eq("Arthur Putey"), eq(41));
        verifyNoMoreInteractions(createdEventReceiver);

        verify(personEvents).createdV2(any(StreamTimestamp.class), eq(id1), eq("Arthur Putey"), eq(41));
        verify(personEvents).createdV1(any(StreamTimestamp.class), eq(id2), eq("Arthur Dent"));
        verify(personEvents).nameUpdated(any(StreamTimestamp.class), eq(id1), eq("Arthur Daley"));
        verify(personEvents).nameUpdated(any(StreamTimestamp.class), eq(id2), eq("Arthur Mumby"));
        verifyNoMoreInteractions(personEvents);
    }

    private StreamTimestamp nextTimestamp() {
        return StreamTimestamp.of("test", timestampStart.plusMillis(timestampOffset.getAndIncrement()));
    }

}
