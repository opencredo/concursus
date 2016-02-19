package com.opencredo.concourse.mapping.events.methods;

import com.opencredo.concourse.domain.events.writing.EventWriter;
import com.opencredo.concourse.domain.events.writing.PublishingEventWriter;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.events.batching.SimpleEventBatch;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;
import com.opencredo.concourse.domain.events.publishing.LoggingEventPublisher;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.annotations.Name;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
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
    private final EventLog eventLog = events -> {
        batchedEvents.add(events);
        return events;
    };
    private final EventWriter eventWriter = PublishingEventWriter.using(eventLog, eventPublisher);

    private final EventBus bus = () -> SimpleEventBatch.writingTo(eventWriter);

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
