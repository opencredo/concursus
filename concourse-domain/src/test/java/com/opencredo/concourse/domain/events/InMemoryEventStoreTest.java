package com.opencredo.concourse.domain.events;

import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.VersionedName;
import com.opencredo.concourse.domain.events.batching.LoggingEventBatch;
import com.opencredo.concourse.domain.events.batching.SimpleEventBatch;
import com.opencredo.concourse.domain.events.consuming.LoggingEventLog;
import com.opencredo.concourse.domain.events.storing.InMemoryEventStore;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.domain.time.TimeRange;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class InMemoryEventStoreTest {

    private final Instant startTime = Instant.now();
    private final Function<Integer, StreamTimestamp> timestamp = i -> StreamTimestamp.of("test", startTime.plusMillis(i));
    private final Tuple empty = TupleSchema.of("empty").makeWith();
    private final InMemoryEventStore eventStore = InMemoryEventStore.empty();

    private final EventBus bus = EventBus.of(() ->
            SimpleEventBatch.writingTo(eventStore.filter(LoggingEventLog::logging))
                .filter(LoggingEventBatch::logging))
            .filter(LoggingEventBus::logging);

    @Test
    public void storesEventsInTimeAscendingOrder() {
        AggregateId aggregateId = AggregateId.of("test", UUID.randomUUID());

        Event created = Event.of(aggregateId, timestamp.apply(10), VersionedName.of("created"), empty);
        Event update1 = Event.of(aggregateId, timestamp.apply(20), VersionedName.of("updated"), empty);
        Event update2 = Event.of(aggregateId, timestamp.apply(30), VersionedName.of("updated"), empty);

        bus.dispatch(batch -> {
            batch.accept(update1);
            batch.accept(update2);
            batch.accept(created);
            batch.accept(created);
        });

        assertThat(eventStore.getEvents(aggregateId), contains(created, update1, update2));
    }

    @Test
    public void filtersEventsByTimeRange() {
        AggregateId aggregateId = AggregateId.of("test", UUID.randomUUID());

        Event created = Event.of(aggregateId, timestamp.apply(10), VersionedName.of("created"), empty);
        Event update1 = Event.of(aggregateId, timestamp.apply(20), VersionedName.of("updated"), empty);
        Event update2 = Event.of(aggregateId, timestamp.apply(30), VersionedName.of("updated"), empty);

        bus.dispatch(batch -> {
            batch.accept(created);
            batch.accept(update1);
            batch.accept(update2);
        });

        assertThat(eventStore.getEvents(aggregateId, TimeRange.fromUnbounded().toExclusive(startTime.plusMillis(30))),
                contains(created, update1));

        assertThat(eventStore.getEvents(aggregateId, TimeRange.fromInclusive(startTime).toExclusive(startTime.plusMillis(30))),
                contains(created, update1));

        assertThat(eventStore.getEvents(aggregateId, TimeRange.fromInclusive(startTime).toInclusive(startTime.plusMillis(30))),
                contains(created, update1, update2));

        assertThat(eventStore.getEvents(aggregateId, TimeRange.fromInclusive(startTime.plusMillis(10)).toUnbounded()),
                contains(created, update1, update2));

        assertThat(eventStore.getEvents(aggregateId, TimeRange.fromExclusive(startTime.plusMillis(10)).toUnbounded()),
                contains(update1, update2));
    }
}
