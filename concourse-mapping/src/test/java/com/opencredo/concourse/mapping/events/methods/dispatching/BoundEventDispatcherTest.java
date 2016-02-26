package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.annotations.Name;
import com.opencredo.concourse.mapping.events.methods.proxying.EventEmittingProxy;
import com.opencredo.concourse.mapping.events.methods.reflection.EventInterfaceInfo;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;

public class BoundEventDispatcherTest {

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

    @HandlesEventsFor("test")
    public interface TestEventsReceiver {

        @Name("created")
        void createdV1(StreamTimestamp timestamp, UUID aggregateId, String name);

        @Name(value="created", version="2")
        void createdVersion2(StreamTimestamp timestamp, UUID aggregateId, int age, String name);

        void nameUpdated(StreamTimestamp timestamp, UUID aggregateId, String updatedName);
    }

    private final TestEventsReceiver handler = mock(TestEventsReceiver.class);

    @Test
    public void dispatchesMethodCallsBasedOnEvents() {
        EventInterfaceInfo<TestEventsReceiver> mapper = EventInterfaceInfo.forInterface(TestEventsReceiver.class);
        BoundEventDispatcher dispatcher = BoundEventDispatcher.binding(mapper.getEventDispatcher(), handler);
        TestEvents emitter = EventEmittingProxy.proxying(dispatcher, TestEvents.class);

        final StreamTimestamp timestamp1 = StreamTimestamp.of("test", Instant.now());
        final StreamTimestamp timestamp2 = StreamTimestamp.of("test", Instant.now());
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        emitter.createdV2(timestamp1, id1, "Arthur Putey", 41);
        emitter.nameUpdated(timestamp2, id2, "Arthur Dent");

        Mockito.verify(handler).createdVersion2(timestamp1, id1, 41, "Arthur Putey");
        Mockito.verify(handler).nameUpdated(timestamp2, id2, "Arthur Dent");
    }

}
