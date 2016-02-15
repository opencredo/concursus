package com.opencredo.concourse.domain.events;

import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.domain.VersionedName;
import com.opencredo.concourse.domain.events.batching.LoggingEventBatch;
import com.opencredo.concourse.domain.events.batching.SubBatchingEventBatch;
import com.opencredo.concourse.domain.events.consuming.EventLog;
import com.opencredo.concourse.domain.events.consuming.LoggingEventLog;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;
import com.opencredo.concourse.domain.events.publishing.LoggingEventPublisher;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class SubBatchingEventBatchTest {

    private final List<Collection<Event>> batchedEvents = new ArrayList<>();
    private final List<Event> publishedEvents = new ArrayList<>();

    private final EventPublisher eventPublisher = LoggingEventPublisher.logging(publishedEvents::add);
    private final EventLog eventLog = LoggingEventLog.logging(batchedEvents::add).publishingTo(eventPublisher);

    private final EventBus eventBus = EventBus.of(() ->
            SubBatchingEventBatch.writingTo(eventLog, 10)
            .filter(LoggingEventBatch::logging))
            .filter(LoggingEventBus::logging);

    @Test
    public void breaksUpBatchIntoSubBatches() {
        Event event = Event.of(
                AggregateId.of("widget", UUID.randomUUID()),
                StreamTimestamp.of("testStream", Instant.now()),
                VersionedName.of("created", "0"),
                TupleSchema.of("test").makeWith()
        );

        eventBus.dispatch(batch -> IntStream.range(0, 23).forEach(i -> batch.accept(event)));

        assertThat(publishedEvents, hasSize(23));
        assertThat(batchedEvents.get(0), hasSize(10));
        assertThat(batchedEvents.get(1), hasSize(10));
        assertThat(batchedEvents.get(2), hasSize(3));
    }

}
