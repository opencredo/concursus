package com.opencredo.concourse.mapping.events.methods;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.batching.ProcessingEventBatch;
import com.opencredo.concourse.domain.events.cataloguing.AggregateCatalogue;
import com.opencredo.concourse.domain.events.cataloguing.InMemoryAggregateCatalogue;
import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.events.filtering.log.EventLogPostFilter;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.events.processing.EventBatchProcessor;
import com.opencredo.concourse.domain.events.processing.PublishingEventBatchProcessor;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.annotations.Initial;
import com.opencredo.concourse.mapping.annotations.Name;
import com.opencredo.concourse.mapping.annotations.Terminal;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

public class ProxyingEventBusTest {


    private final List<Collection<Event>> batchedEvents = new ArrayList<>();
    private final List<Event> publishedEvents = new ArrayList<>();
    private final AggregateCatalogue aggregateCatalogue = new InMemoryAggregateCatalogue();
    private final EventLogPostFilter postFilter = (log, events) -> {
        events.forEach(aggregateCatalogue);
        return events;
    };

    private final EventPublisher eventPublisher = publishedEvents::add;
    private final EventLog eventLog = postFilter.apply(EventLog.loggingTo(batchedEvents::add));
    private final EventBatchProcessor batchProcessor = PublishingEventBatchProcessor.using(eventLog, eventPublisher);

    private final EventBus bus = () -> ProcessingEventBatch.processingWith(batchProcessor);

    private final ProxyingEventBus unit = ProxyingEventBus.proxying(bus);

    @HandlesEventsFor("test")
    public interface TestEvents {

        @Initial
        @Name("created")
        void createdV1(StreamTimestamp timestamp, UUID aggregateId, String name);

        @Initial
        @Name(value="created", version="2")
        void createdV2(StreamTimestamp timestamp, UUID aggregateId, String name, int age);

        void nameUpdated(StreamTimestamp timestamp, UUID aggregateId, @Name("updatedName") String newName);

        @Terminal
        void deleted(StreamTimestamp ts, UUID aggregateId);
    }

    @Test
    public void proxiesMethodCallsToEventBus() {
        Instant eventTime = Instant.now();
        UUID aggregateId = UUID.randomUUID();

        unit.dispatch(TestEvents.class, e -> {
            e.createdV2(StreamTimestamp.of("test", eventTime), aggregateId, "Arthur Putey", 41);
            e.nameUpdated(StreamTimestamp.of("test", eventTime.plusMillis(1)), aggregateId, "Arthur Dent");
        });

        assertThat(batchedEvents, hasSize(1));
        assertThat(batchedEvents.get(0), hasSize(2));
        assertThat(publishedEvents, hasSize(2));
        assertThat(aggregateCatalogue.getUuids("test"), contains(aggregateId));
    }

}
