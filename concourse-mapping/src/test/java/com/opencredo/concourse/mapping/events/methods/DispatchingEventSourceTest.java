package com.opencredo.concourse.mapping.events.methods;

import com.opencredo.concourse.domain.events.batching.SimpleEventBatch;
import com.opencredo.concourse.domain.events.caching.InMemoryEventStore;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.writing.EventWriter;
import com.opencredo.concourse.domain.events.writing.PublishingEventWriter;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.annotations.Name;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingEventSource;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import org.junit.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DispatchingEventSourceTest {

    @HandlesEventsFor("test")
    public interface CreatedEventReceiver {

        @Name(value = "created", version = "2")
        void created(StreamTimestamp timestamp, UUID aggregateId, String name, int age);

    }

    @HandlesEventsFor("test")
    public interface TestEvents {

        @Name("created")
        void createdV1(StreamTimestamp timestamp, UUID aggregateId, String name);

        @Name(value="created", version="2")
        void createdV2(StreamTimestamp timestamp, UUID aggregateId, String name, int age);

        void nameUpdated(StreamTimestamp timestamp, UUID aggregateId, @Name("updatedName") String newName);
    }

    @Test
    public void useWithReplayer() {
        InMemoryEventStore eventStore = InMemoryEventStore.empty();
        EventWriter eventWriter = PublishingEventWriter.using(eventStore, event -> {});
        EventSource eventSource = eventStore.getEventSource();

        ProxyingEventBus eventBus = ProxyingEventBus.proxying(() -> SimpleEventBatch.writingTo(eventWriter));

        final StreamTimestamp timestamp1 = StreamTimestamp.of("test", Instant.now());
        final StreamTimestamp timestamp2 = StreamTimestamp.of("test", Instant.now());
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        eventBus.dispatch(TestEvents.class, batch -> {
            batch.createdV2(timestamp1, id1, "Arthur Putey", 41);
            batch.nameUpdated(timestamp2, id2, "Arthur Dent");
        });

        Optional<String> name = DispatchingEventSource
                .dispatching(eventSource, CreatedEventReceiver.class)
                .preload(id1)
                .replaying(id1)
                .collectFirst(caller -> (ts, id, n, age) -> caller.accept(n));

        assertThat(name, equalTo(Optional.of("Arthur Putey")));
    }

}
