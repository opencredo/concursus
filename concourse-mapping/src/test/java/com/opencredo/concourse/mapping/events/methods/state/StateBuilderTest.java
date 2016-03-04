package com.opencredo.concourse.mapping.events.methods.state;

import com.opencredo.concourse.domain.events.batching.SimpleEventBatch;
import com.opencredo.concourse.domain.events.caching.CachingEventSource;
import com.opencredo.concourse.domain.events.caching.InMemoryEventStore;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.writing.EventWriter;
import com.opencredo.concourse.domain.events.writing.PublishingEventWriter;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEvent;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.events.methods.helper.PersonEvents;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class StateBuilderTest {

    private final InMemoryEventStore eventStore = InMemoryEventStore.empty();
    private final EventWriter eventWriter = PublishingEventWriter.using(eventStore, event -> {});
    private final EventSource eventSource = CachingEventSource.retrievingWith(eventStore);

    private final ProxyingEventBus eventBus = ProxyingEventBus.proxying(() -> SimpleEventBatch.writingTo(eventWriter));

    @HandlesEventsFor("person")
    public static final class PersonState {

        @HandlesEvent("created")
        public static PersonState created(UUID personId, String name) {
            return new PersonState(personId, name, Optional.empty());
        }

        @HandlesEvent(value = "created", version = "2")
        public static PersonState created(UUID personId, String name, int age) {
            return new PersonState(personId, name, Optional.of(age));
        }

        private final UUID id;
        private String name;
        private final Optional<Integer> age;

        public PersonState(UUID id, String name, Optional<Integer> age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        @HandlesEvent
        public void nameUpdated(String updatedName) {
            name = updatedName;
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Optional<Integer> getAge() {
            return age;
        }
    }

    private final Instant timestampStart = Instant.now();
    private final AtomicInteger timestampOffset = new AtomicInteger(0);

    @Test
    public void buildStates() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        eventBus.dispatch(PersonEvents.class, batch -> {
            batch.createdV2(nextTimestamp(), id1, "Arthur Putey", 41);
            batch.createdV1(nextTimestamp(), id2, "Arthur Dent");
            batch.nameUpdated(nextTimestamp(), id1, "Arthur Daley");
            batch.nameUpdated(nextTimestamp(), id2, "Arthur Mumby");
        });

        Map<UUID, PersonState> states = StateBuilder.forStateClass(PersonState.class)
                .buildStates(eventSource, Arrays.asList(id1, id2));

        assertThat(states.get(id1).getName(), equalTo("Arthur Daley"));
        assertThat(states.get(id2).getName(), equalTo("Arthur Mumby"));
    }

    private StreamTimestamp nextTimestamp() {
        return StreamTimestamp.of("test", timestampStart.plusMillis(timestampOffset.getAndIncrement()));
    }

}
