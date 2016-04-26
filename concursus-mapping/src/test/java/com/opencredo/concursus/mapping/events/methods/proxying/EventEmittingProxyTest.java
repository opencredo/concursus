package com.opencredo.concursus.mapping.events.methods.proxying;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.annotations.Name;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EventEmittingProxyTest {

    @HandlesEventsFor("test")
    public interface TestEvents {

        @Name("created")
        void createdV1(StreamTimestamp timestamp, String aggregateId, String name);

        @Name(value="created", version="2")
        void createdV2(StreamTimestamp timestamp, String aggregateId, String name, int age);

        void nameUpdated(StreamTimestamp timestamp, String aggregateId, @Name("updatedName") String newName);
    }

    @Test
    public void emitsEventsBasedOnMethodCalls() {
        List<Event> emittedEvents = new ArrayList<>();

        TestEvents emitter = EventEmittingProxy.proxying(emittedEvents::add, TestEvents.class);

        emitter.createdV2(StreamTimestamp.of("test", Instant.now()), UUID.randomUUID().toString(), "Arthur Putey", 41);
        emitter.nameUpdated(StreamTimestamp.of("test", Instant.now()), UUID.randomUUID().toString(), "Arthur Dent");

        assertThat(emittedEvents.get(0).getParameters().toString(), equalTo("test/created_2{age=41, name=Arthur Putey}"));
        assertThat(emittedEvents.get(1).getParameters().toString(), equalTo("test/nameUpdated_0{updatedName=Arthur Dent}"));
    }

}
