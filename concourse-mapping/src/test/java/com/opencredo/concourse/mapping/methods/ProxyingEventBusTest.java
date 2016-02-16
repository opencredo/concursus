package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventBus;
import com.opencredo.concourse.domain.events.LoggingEventBus;
import com.opencredo.concourse.domain.events.batching.LoggingEventBatch;
import com.opencredo.concourse.domain.events.batching.SimpleEventBatch;
import com.opencredo.concourse.domain.events.consuming.EventLog;
import com.opencredo.concourse.domain.events.consuming.LoggingEventLog;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;
import com.opencredo.concourse.domain.events.publishing.LoggingEventPublisher;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.annotations.Name;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ProxyingEventBusTest {


    private final List<Collection<Event>> batchedEvents = new ArrayList<>();
    private final List<Event> publishedEvents = new ArrayList<>();

    private final EventPublisher eventPublisher = LoggingEventPublisher.logging(publishedEvents::add);
    private final EventLog eventLog = LoggingEventLog.logging(events -> {
        batchedEvents.add(events);
        return events;
    }).publishingTo(eventPublisher);

    private final EventBus bus = LoggingEventBus.logging(() ->
            LoggingEventBatch.logging(SimpleEventBatch.writingTo(eventLog)));

    private final ProxyingEventBus unit = ProxyingEventBus.proxying(bus);

    @HandlesEventsFor("test")
    public interface TestEvents {

        @Name("created")
        void createdV1(StreamTimestamp timestamp, UUID aggregateId, String name);

        @Name(value="created", version="2")
        void createdV2(StreamTimestamp timestamp, UUID aggregateId, String name, int age);

        void nameUpdated(StreamTimestamp timestamp, UUID aggregateId, @Name("updatedName") String newName);
    }

    @Test
    public void proxiesMethodCallsToEventBus() {
        Instant eventTime = Instant.now();
        UUID aggregateId = UUID.randomUUID();

        unit.dispatch(TestEvents.class, e -> {
            e.createdV2(StreamTimestamp.of("test", eventTime), aggregateId, "Arthur Putey", 41);
            e.nameUpdated(StreamTimestamp.of("test", eventTime.plusMillis(1)), aggregateId, "Arthur Dent");
        });
    }
}
