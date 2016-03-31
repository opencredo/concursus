package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.events.sourcing.EventSource;
import com.opencredo.concursus.domain.state.StateRepository;
import com.opencredo.concursus.domain.storing.EventStore;
import com.opencredo.concursus.domain.storing.InMemoryEventStore;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEvent;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.annotations.Initial;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concursus.mapping.events.methods.state.DispatchingStateRepository;
import org.junit.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LightbulbExample {

    @HandlesEventsFor("lightbulb")
    public interface LightbulbEvents {
        @Initial
        void created(StreamTimestamp timestamp, UUID id);
        void screwedIn(StreamTimestamp timestamp, UUID id, String location);
        void switchedOn(StreamTimestamp timestamp, UUID id);
        void switchedOff(StreamTimestamp timestamp, UUID id);
        void unscrewed(StreamTimestamp timestamp, UUID id);
    }

    @HandlesEventsFor("lightbulb")
    public static final class LightbulbState {

        @HandlesEvent
        public static LightbulbState created(UUID id) {
            return new LightbulbState(id);
        }

        private final UUID id;
        private Optional<String> screwedInLocation = Optional.empty();
        private boolean switchedOn = false;

        public LightbulbState(UUID id) {
            this.id = id;
        }

        @HandlesEvent
        public void screwedIn(String location) {
            screwedInLocation = Optional.of(location);
        }

        @HandlesEvent
        public void unscrewed() {
            screwedInLocation = Optional.empty();
        }

        @HandlesEvent
        public void switchedOn() {
            switchedOn = true;
        }

        @HandlesEvent
        public void switchedOff() {
            switchedOn = false;
        }

        public UUID getId() {
            return id;
        }

        public boolean isSwitchedOn() {
            return switchedOn;
        }

        public Optional<String> getScrewedInLocation() {
            return screwedInLocation;
        }

    }

    private final EventStore eventStore = InMemoryEventStore.empty();

    private final EventSource eventSource = EventSource.retrievingWith(eventStore);
    private final EventLog eventLog = EventLog.loggingTo(eventStore);

    private final ProxyingEventBus eventBus = ProxyingEventBus.proxying(
            EventBus.processingWith(EventBatchProcessor.loggingWith(eventLog)));

    private final StateRepository<LightbulbState> lightbulbStateRepository = DispatchingStateRepository.using(
            eventSource, LightbulbState.class);

    @Test
    public void createAndRetrieveState() {
        UUID lightbulbId = UUID.randomUUID();
        Instant start = Instant.now().minus(3, DAYS);

        recordEventHistory(lightbulbId, start);

        LightbulbState lightbulbState = lightbulbStateRepository.getState(lightbulbId).get();

        assertThat(lightbulbState.getId(), equalTo(lightbulbId));
        assertTrue(lightbulbState.isSwitchedOn());
        assertThat(lightbulbState.getScrewedInLocation(), equalTo(Optional.of("hall")));
    }

    @Test
    public void timeTravelling() {
        UUID lightbulbId = UUID.randomUUID();
        Instant start = Instant.now().minus(3, DAYS);

        recordEventHistory(lightbulbId, start);

        // Lightbulb is still in the kitchen, and is switched off.
        LightbulbState lightbulbState = lightbulbStateRepository.getState(lightbulbId, start.plus(4, HOURS)).get();

        assertThat(lightbulbState.getId(), equalTo(lightbulbId));
        assertFalse(lightbulbState.isSwitchedOn());
        assertThat(lightbulbState.getScrewedInLocation(), equalTo(Optional.of("kitchen")));
    }

    private void recordEventHistory(UUID lightbulbId, Instant start) {
        eventBus.dispatch(LightbulbEvents.class, lightbulb -> {
            // The lightbulb is created.
            lightbulb.created(StreamTimestamp.of(start), lightbulbId);

            // The lightbulb is screwed in, in the kitchen, and switched on and off a few times.
            lightbulb.screwedIn(StreamTimestamp.of(start.plus(1, MINUTES)), lightbulbId, "kitchen");
            lightbulb.switchedOn(StreamTimestamp.of(start.plus(2, HOURS)), lightbulbId);
            lightbulb.switchedOff(StreamTimestamp.of(start.plus(3, HOURS)), lightbulbId);
            lightbulb.switchedOn(StreamTimestamp.of(start.plus(4, HOURS)), lightbulbId);
            lightbulb.switchedOff(StreamTimestamp.of(start.plus(5, HOURS)), lightbulbId);

            // The lightbulb is unscrewed from the kitchen, and screwed in, in the hall.
            lightbulb.unscrewed(StreamTimestamp.of(start.plus(1, DAYS)), lightbulbId);
            lightbulb.screwedIn(StreamTimestamp.of(start.plus(1, DAYS).plus(10, MINUTES)), lightbulbId, "hall");

            // The lightbulb is switched on again.
            lightbulb.switchedOn(StreamTimestamp.of(start.plus(1, DAYS).plus(15, MINUTES)), lightbulbId);
        });
    }
}
