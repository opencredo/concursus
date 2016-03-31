package com.opencredo.concursus.mapping.events.methods.state;

import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.events.sourcing.EventSource;
import com.opencredo.concursus.domain.state.StateRepository;
import com.opencredo.concursus.domain.storing.InMemoryEventStore;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEvent;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.events.methods.helper.PersonEvents;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import org.junit.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DispatchingStateRepositoryTest {

    private final InMemoryEventStore eventStore = InMemoryEventStore.empty();
    private final EventSource eventSource = EventSource.retrievingWith(eventStore);

    private final ProxyingEventBus eventBus = ProxyingEventBus.proxying(EventBus.processingWith(EventBatchProcessor.forwardingTo(eventStore)));

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

        StreamTimestamp createTimestamp = nextTimestamp();
        StreamTimestamp updateTimestamp = nextTimestamp();

        eventBus.dispatch(PersonEvents.class, batch -> {
            batch.createdV2(createTimestamp, id1, "Arthur Putey", 41);
            batch.createdV1(createTimestamp, id2, "Arthur Dent");
            batch.nameUpdated(updateTimestamp, id1, "Arthur Daley");
            batch.nameUpdated(updateTimestamp, id2, "Arthur Mumby");
        });

        StateRepository<PersonState> cache = DispatchingStateRepository.using(eventSource, PersonState.class);

        Map<UUID, PersonState> statesBeforeUpdate = cache.getStates(id1, id2);

        assertThat(statesBeforeUpdate.get(id1).getName(), equalTo("Arthur Daley"));
        assertThat(statesBeforeUpdate.get(id2).getName(), equalTo("Arthur Mumby"));

        eventBus.dispatch(PersonEvents.class, batch -> {
            batch.nameUpdated(updateTimestamp.subStream("substream"), id2, "Arthur, King of the Britons");
        });

        Map<UUID, PersonState> states = cache.getStates(id1, id2);

        assertThat(states.get(id1).getName(), equalTo("Arthur Daley"));
        assertThat(states.get(id2).getName(), equalTo("Arthur, King of the Britons"));
    }

    private StreamTimestamp nextTimestamp() {
        return StreamTimestamp.of("test", timestampStart.plusMillis(timestampOffset.getAndIncrement()));
    }

}
