package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.events.sourcing.EventSource;
import com.opencredo.concursus.domain.events.state.StateRepository;
import com.opencredo.concursus.domain.events.storage.EventStore;
import com.opencredo.concursus.domain.events.storage.InMemoryEventStore;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEvent;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.annotations.Initial;
import com.opencredo.concursus.mapping.annotations.Terminal;
import com.opencredo.concursus.mapping.events.methods.dispatching.DispatchingEventOutChannel;
import com.opencredo.concursus.mapping.events.methods.proxying.EventEmittingProxy;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concursus.mapping.events.methods.state.DispatchingStateRepository;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.time.temporal.ChronoUnit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LightbulbExample {

    @HandlesEventsFor("lightbulb")
    public interface LightbulbEvents {
        @Initial
        void created(StreamTimestamp timestamp, String id, int wattage);
        void screwedIn(StreamTimestamp timestamp, String id, String location);
        void switchedOn(StreamTimestamp timestamp, String id);
        void switchedOff(StreamTimestamp timestamp, String id);
        void unscrewed(StreamTimestamp timestamp, String id);
        @Terminal
        void blown(StreamTimestamp timestamp, String id);
    }

    @HandlesEventsFor("lightbulb")
    public static final class LightbulbState {

        @HandlesEvent
        public static LightbulbState created(String id, int wattage) {
            return new LightbulbState(id, wattage);
        }

        private final String id;
        private final int wattage;
        private Optional<String> screwedInLocation = Optional.empty();
        private boolean switchedOn = false;

        public LightbulbState(String id, int wattage) {
            this.id = id;
            this.wattage = wattage;
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

        public String getId() {
            return id;
        }

        public int getWattage() {
            return wattage;
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
        String lightbulbId = "id1";
        Instant start = Instant.now().minus(3, DAYS);

        recordEventHistory(lightbulbId, start);

        LightbulbState lightbulbState = lightbulbStateRepository.getState(lightbulbId).get();

        assertThat(lightbulbState.getId(), equalTo(lightbulbId));
        assertTrue(lightbulbState.isSwitchedOn());
        assertThat(lightbulbState.getScrewedInLocation(), equalTo(Optional.of("hall")));
    }

    @Test
    public void timeTravelling() {
        String lightbulbId = "id1";
        Instant start = Instant.now().minus(3, DAYS);

        recordEventHistory(lightbulbId, start);

        // Lightbulb is still in the kitchen, and is switched off.
        LightbulbState lightbulbState = lightbulbStateRepository.getState(lightbulbId, start.plus(4, HOURS)).get();

        assertThat(lightbulbState.getId(), equalTo(lightbulbId));
        assertFalse(lightbulbState.isSwitchedOn());
        assertThat(lightbulbState.getScrewedInLocation(), equalTo(Optional.of("kitchen")));
    }

    private void recordEventHistory(String lightbulbId, Instant start) {
        eventBus.dispatch(LightbulbEvents.class, lightbulb -> {
            // The lightbulb is created.
            lightbulb.created(StreamTimestamp.of(start), lightbulbId, 60);

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

    @Test
    public void emitEventsToConsole() {
        List<Event> collectedEvents = new ArrayList<>();
        LightbulbEvents events = EventEmittingProxy.proxying(
                collectedEvents::add,
                LightbulbEvents.class);

        String lightbulbId = "id1";
        StreamTimestamp start = StreamTimestamp.now();
        events.created(start, lightbulbId, 60);
        events.screwedIn(start.plus(1, MINUTES), lightbulbId, "hallway");
        events.switchedOn(start.plus(2, MINUTES), lightbulbId);

        LightbulbEvents handler = Mockito.mock(LightbulbEvents.class);
        Consumer<Event> eventConsumer = DispatchingEventOutChannel.toHandler(LightbulbEvents.class, handler);
        collectedEvents.forEach(eventConsumer);
    }

    public void replayToHandler(List<Event> collectedEvents, LightbulbEvents handler) {
        Consumer<Event> eventConsumer = DispatchingEventOutChannel.toHandler(LightbulbEvents.class, handler);
        collectedEvents.forEach(eventConsumer);
    }

    public static final class PowerUsageCalculator implements LightbulbEvents {

        private long wattage = 0;
        private long millisecondsUsage = 0;
        private Optional<Instant> lastSwitchedOn;

        @Override
        public void created(StreamTimestamp timestamp, String id, int wattage) {
            this.wattage = wattage;
        }

        @Override
        public void screwedIn(StreamTimestamp timestamp, String id, String location) {

        }

        @Override
        public void switchedOn(StreamTimestamp timestamp, String id) {
            lastSwitchedOn = Optional.of(timestamp.getTimestamp());
        }

        @Override
        public void switchedOff(StreamTimestamp timestamp, String id) {
            lastSwitchedOn.ifPresent(switchedOnTime -> millisecondsUsage += Duration.between(timestamp.getTimestamp(), switchedOnTime).toMillis());
        }

        @Override
        public void unscrewed(StreamTimestamp timestamp, String id) {

        }

        @Override
        public void blown(StreamTimestamp timestamp, String id) {

        }

        public double getKilowattHours() {
            return (double) millisecondsUsage * ((double) wattage / 1000) / Duration.ofHours(1).toMillis();
        }
    }
}
