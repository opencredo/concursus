package com.opencredo.concursus.domain.events.dispatching;

import com.opencredo.concursus.data.tuples.TupleSchema;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.events.processing.PublishingEventBatchProcessor;
import com.opencredo.concursus.domain.events.publishing.EventPublisher;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class EventBusTest {

    private final List<Collection<Event>> loggedEvents = new ArrayList<>();
    private final List<Event> publishedEvents = new ArrayList<>();

    private final EventPublisher eventPublisher = publishedEvents::add;
    private final EventLog eventLog = events -> {
        loggedEvents.add(events);
        return events;
    };
    private final EventBatchProcessor batchProcessor = PublishingEventBatchProcessor.using(eventLog, eventPublisher);

    private final EventBus bus = EventBus.processingWith(batchProcessor);

    @Test
    public void dispatchesEventsSinglyToLogAndPublisher() {
        Event event1 = Event.of(
                AggregateId.of("widget", UUID.randomUUID()),
                StreamTimestamp.of("testStream", Instant.now()),
                VersionedName.of("created", "0"),
                TupleSchema.of("test").makeWith()
        );

        Event event2 = Event.of(
                AggregateId.of("widget", UUID.randomUUID()),
                StreamTimestamp.of("testStream", Instant.now()),
                VersionedName.of("created", "0"),
                TupleSchema.of("test").makeWith()
        );

        bus.accept(event1);
        bus.accept(event2);

        assertThat(loggedEvents.get(0), contains(event1));
        assertThat(loggedEvents.get(1), contains(event2));
        assertThat(publishedEvents, contains(event1, event2));
    }

    @Test
    public void dispatchesEventsInBatchToLogAndPublisher() {
        Event event1 = Event.of(
                AggregateId.of("widget", UUID.randomUUID()),
                StreamTimestamp.of("testStream", Instant.now()),
                VersionedName.of("created", "0"),
                TupleSchema.of("test").makeWith()
        );

        Event event2 = Event.of(
                AggregateId.of("widget", UUID.randomUUID()),
                StreamTimestamp.of("testStream", Instant.now()),
                VersionedName.of("created", "0"),
                TupleSchema.of("test").makeWith()
        );

        bus.dispatch(batch -> {
            batch.accept(event1);
            batch.accept(event2);
        });

        assertThat(loggedEvents.get(0), contains(event1, event2));
        assertThat(publishedEvents, contains(event1, event2));
    }

}
