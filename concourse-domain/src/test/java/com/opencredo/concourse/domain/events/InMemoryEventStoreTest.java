package com.opencredo.concourse.domain.events;

import com.google.common.collect.ImmutableMap;
import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.domain.events.batching.ProcessingEventBatch;
import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.events.processing.EventBatchProcessor;
import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.storing.InMemoryEventStore;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.domain.time.TimeRange;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

public class InMemoryEventStoreTest {

    private final Instant startTime = Instant.now();
    private final Function<Integer, StreamTimestamp> timestamp = i -> StreamTimestamp.of("test", startTime.plusMillis(i));
    private final TupleSchema emptySchema = TupleSchema.of("empty");
    private final Tuple empty = emptySchema.makeWith();
    private final EventTypeMatcher eventTypeMatcher = et -> Optional.of(emptySchema);

    private final InMemoryEventStore eventStore = InMemoryEventStore.empty();
    private final EventSource eventSource = EventSource.retrievingWith(eventStore);

    private final EventBus bus = () -> ProcessingEventBatch.processingWith(EventBatchProcessor.forwardingTo(eventStore));

    @Test
    public void storesEventsInTimeDescendingOrder() {
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

        assertRetrieved(eventSource.getEvents(eventTypeMatcher, aggregateId), update2, update1, created);
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

        assertRetrieved(eventStore.getEvents(eventTypeMatcher, aggregateId, TimeRange.fromUnbounded().toExclusive(startTime.plusMillis(30))),
                update1, created);

        assertRetrieved(eventStore.getEvents(eventTypeMatcher, aggregateId, TimeRange.fromInclusive(startTime).toExclusive(startTime.plusMillis(30))),
                update1, created);

        assertRetrieved(eventStore.getEvents(eventTypeMatcher, aggregateId, TimeRange.fromInclusive(startTime).toInclusive(startTime.plusMillis(30))),
                update2, update1, created);

        assertRetrieved(eventStore.getEvents(eventTypeMatcher, aggregateId, TimeRange.fromInclusive(startTime.plusMillis(10)).toUnbounded()),
                update2, update1, created);

        assertRetrieved(eventStore.getEvents(eventTypeMatcher, aggregateId, TimeRange.fromExclusive(startTime.plusMillis(10)).toUnbounded()),
                update2, update1);
    }

    private Collection<Event> stripProcessingTimes(Collection<Event> events) {
        return events.stream().map(event ->
                Event.of(event.getAggregateId(), event.getEventTimestamp(), event.getEventName(), event.getParameters()))
                .collect(Collectors.toList());
    }

    @Test
    public void preloadsEvents() {
        AggregateId aggregateId1 = AggregateId.of("test", UUID.randomUUID());
        AggregateId aggregateId2 = AggregateId.of("test", UUID.randomUUID());
        AggregateId aggregateId3 = AggregateId.of("test", UUID.randomUUID());

        Event created1 = Event.of(aggregateId1, timestamp.apply(10), VersionedName.of("created"), empty);
        Event created2 = Event.of(aggregateId2, timestamp.apply(20), VersionedName.of("created"), empty);
        Event created3 = Event.of(aggregateId3, timestamp.apply(30), VersionedName.of("created"), empty);

        bus.dispatch(batch -> {
            batch.accept(created1);
            batch.accept(created2);
            batch.accept(created3);
        });

        CachedEventSource preloaded = eventSource.preload(eventTypeMatcher, "test", Arrays.asList(aggregateId1.getId(), aggregateId3.getId()),
                TimeRange.fromUnbounded().toExclusive(startTime.plusMillis(30)));

        assertRetrieved(preloaded.getEvents(aggregateId1), created1);
        assertThat(preloaded.getEvents(aggregateId2), hasSize(0));
        assertThat(preloaded.getEvents(aggregateId3), hasSize(0));
    }

    private void assertRetrieved(Collection<Event> processedEvents, Event...events) {
        assertThat(stripProcessingTimes(processedEvents), contains(events));
    }

    @Test
    public void filtersByMatchedEventTypes() {
        AggregateId aggregateId = AggregateId.of("test", UUID.randomUUID());

        Event created = Event.of(aggregateId, timestamp.apply(10), VersionedName.of("created"), empty);
        Event update1 = Event.of(aggregateId, timestamp.apply(20), VersionedName.of("updated"), empty);
        Event update2 = Event.of(aggregateId, timestamp.apply(30), VersionedName.of("updated"), empty);

        bus.dispatch(batch -> {
            batch.accept(created);
            batch.accept(update1);
            batch.accept(update2);
        });

        final EventTypeMatcher updateOnlyTypeMatcher = EventTypeMatcher.matchingAgainst(ImmutableMap.of(
                EventType.of(update1), emptySchema
        ));

        assertRetrieved(eventSource.getEvents(updateOnlyTypeMatcher, aggregateId), update2, update1);
    }
}
