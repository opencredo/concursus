package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.domain.events.batching.SimpleEventBatch;
import com.opencredo.concourse.domain.events.storing.InMemoryEventStore;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.annotations.Name;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DispatchingPreloadableEventSourceTest {

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

        ProxyingEventBus eventBus = ProxyingEventBus.proxying(() -> SimpleEventBatch.writingTo(eventStore));

        final StreamTimestamp timestamp1 = StreamTimestamp.of("test", Instant.now());
        final StreamTimestamp timestamp2 = StreamTimestamp.of("test", Instant.now());
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        eventBus.dispatch(TestEvents.class, batch -> {
            batch.createdV2(timestamp1, id1, "Arthur Putey", 41);
            batch.nameUpdated(timestamp2, id2, "Arthur Dent");
        });

        Optional<String> name = DispatchingPreloadableEventSource
                .dispatching(eventStore, CreatedEventReceiver.class)
                .preload(Arrays.asList(id1))
                .replaying(id1)
                .collectFirst(caller -> (ts, id, n, age) -> caller.accept(n));

        assertThat(name, equalTo(Optional.of("Arthur Putey")));
    }

}
