package com.opencredo.concursus.mapping.events.methods.dispatching;

import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.events.sourcing.EventSource;
import com.opencredo.concursus.domain.events.storage.InMemoryEventStore;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.annotations.Initial;
import com.opencredo.concursus.mapping.annotations.Name;
import com.opencredo.concursus.mapping.events.methods.helper.PersonEvents;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import org.junit.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class DispatchingEventSourceTest {

    @HandlesEventsFor("person")
    public interface CreatedEventReceiver {

        @Initial
        @Name(value = "created", version = "2")
        void created(StreamTimestamp timestamp, String aggregateId, String name, int age);

    }

    private final InMemoryEventStore eventStore = InMemoryEventStore.empty();

    private final EventSource eventSource = EventSource.retrievingWith(eventStore);
    private final DispatchingEventSource<PersonEvents> testEventDispatchingEventSource = DispatchingEventSourceFactory.dispatching(eventSource).dispatchingTo(PersonEvents.class);
    private final DispatchingEventSource<CreatedEventReceiver> createdEventDispatchingEventSource = DispatchingEventSourceFactory.dispatching(eventSource).dispatchingTo(CreatedEventReceiver.class);
    private final ProxyingEventBus eventBus = ProxyingEventBus.proxying(EventBus.processingWith(EventBatchProcessor.forwardingTo(eventStore)));
    private final Function<Consumer<String>, PersonEvents> nameCollector = caller -> new PersonEvents() {
        @Override
        public void createdV1(StreamTimestamp timestamp, String aggregateId, String name) {
            caller.accept(name);
        }

        @Override
        public void createdV2(StreamTimestamp timestamp, String aggregateId, String name, int age) {
            caller.accept(name);
        }

        @Override
        public void nameUpdated(StreamTimestamp timestamp, String aggregateId, @Name("updatedName") String newName) {
            caller.accept(newName);
        }
    };

    private final Instant timestampStart = Instant.now();
    private final AtomicInteger timestampOffset = new AtomicInteger(0);

    @Test
    public void replayAndCollectFirst() {
        String id1 = "id1";
        String id2 = "id2";

        eventBus.dispatch(PersonEvents.class, batch -> {
            batch.createdV2(nextTimestamp(), id1, "Arthur Putey", 41);
            batch.createdV1(nextTimestamp(), id2, "Arthur Dent");
            batch.nameUpdated(nextTimestamp(), id1, "Arthur Daley");
            batch.nameUpdated(nextTimestamp(), id2, "Arthur Mumby");
        });

        assertThat(createdEventDispatchingEventSource.replaying(id1).<String>collectFirst(
                caller -> (ts, id, n, age) -> caller.accept(n)), equalTo(Optional.of("Arthur Putey")));

        assertThat(createdEventDispatchingEventSource.replaying(id2).<String>collectFirst(
                caller -> (ts, id, n, age) -> caller.accept(n)), equalTo(Optional.empty()));
    }

    @Test
    public void replayAndCollectAll() {
        String id1 = "id1";
        String id2 = "id2";

        eventBus.dispatch(PersonEvents.class, batch -> {
            batch.createdV2(nextTimestamp(), id1, "Arthur Putey", 41);
            batch.createdV1(nextTimestamp(), id2, "Arthur Dent");
            batch.nameUpdated(nextTimestamp(), id1, "Arthur Daley");
            batch.nameUpdated(nextTimestamp(), id2, "Arthur Mumby");
        });

        assertThat(testEventDispatchingEventSource.replaying(id1).inAscendingOrder().collectAll(nameCollector),
                contains("Arthur Putey", "Arthur Daley"));
        assertThat(testEventDispatchingEventSource.replaying(id2).inAscendingOrder().collectAll(nameCollector),
                contains("Arthur Dent", "Arthur Mumby"));
    }

    @Test
    public void preloadThenReplayAndCollectOne() {
        String id1 = "id1";
        String id2 = "id2";

        eventBus.dispatch(PersonEvents.class, batch -> {
            batch.createdV2(nextTimestamp(), id1, "Arthur Putey", 41);
            batch.createdV1(nextTimestamp(), id2, "Arthur Dent");
            batch.nameUpdated(nextTimestamp(), id1, "Arthur Daley");
            batch.nameUpdated(nextTimestamp(), id2, "Arthur Mumby");
        });

        DispatchingCachedEventSource<CreatedEventReceiver> cached = createdEventDispatchingEventSource.preload(id1, id2);

        assertThat(cached.replaying(id1).<String>collectFirst(
                caller -> (ts, id, n, age) -> caller.accept(n)), equalTo(Optional.of("Arthur Putey")));

        assertThat(cached.replaying(id2).<String>collectFirst(
                caller -> (ts, id, n, age) -> caller.accept(n)), equalTo(Optional.empty()));
    }

    @Test
    public void preloadThenReplayAndCollectAll() {
        String id1 = "id1";
        String id2 = "id2";

        eventBus.dispatch(PersonEvents.class, batch -> {
            batch.createdV2(nextTimestamp(), id1, "Arthur Putey", 41);
            batch.createdV1(nextTimestamp(), id2, "Arthur Dent");
            batch.nameUpdated(nextTimestamp(), id1, "Arthur Daley");
            batch.nameUpdated(nextTimestamp(), id2, "Arthur Mumby");
        });

        DispatchingCachedEventSource<PersonEvents> cached = testEventDispatchingEventSource.preload(id1, id2);

        assertThat(cached.replaying(id1).inAscendingOrder().collectAll(nameCollector),
                contains("Arthur Putey", "Arthur Daley"));
        assertThat(cached.replaying(id2).inAscendingOrder().collectAll(nameCollector),
                contains("Arthur Dent", "Arthur Mumby"));
    }

    private StreamTimestamp nextTimestamp() {
        return StreamTimestamp.of("test", timestampStart.plusMillis(timestampOffset.getAndIncrement()));
    }

}
