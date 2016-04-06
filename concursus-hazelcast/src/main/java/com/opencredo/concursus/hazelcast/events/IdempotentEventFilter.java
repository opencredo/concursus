package com.opencredo.concursus.hazelcast.events;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.opencredo.concursus.domain.events.Event;

import java.util.Map;
import java.util.function.Predicate;

/**
 * A filter on events that discards events with a duplicate aggregate id/event timestamp identity within a given time
 * window.
 */
public final class IdempotentEventFilter implements Predicate<Event> {

    /**
     * Add configuration for an {@link IMap} that will contain the event window.
     * @param eventWindowName The name of the {@link IMap} to configure.
     * @param timeToLiveSeconds The number of seconds to keep an event's identity in the window before discarding it.
     * @param config The {@link Config} to add the {@link IMap} configuration to.
     * @return The updated {@link Config}
     */
    public static Config configureCache(String eventWindowName, int timeToLiveSeconds, Config config) {
        config.addMapConfig(new MapConfig(eventWindowName).setTimeToLiveSeconds(timeToLiveSeconds));
        return config;
    }

    /**
     * Create a {@link Predicate} filtering events using the event window configured in the supplied
     * {@link HazelcastInstance}.
     * @param hazelcastInstance The {@link HazelcastInstance} that supplies the event window.
     * @param eventWindowName The name of the {@link IMap} that contains the event window data.
     * @return The constructed {@link Predicate}.
     */
    public static Predicate<Event> using(HazelcastInstance hazelcastInstance, String eventWindowName) {
        return new IdempotentEventFilter(hazelcastInstance.getMap(eventWindowName));
    }

    private final Map<SerializableEventIdentity, Boolean> eventsWindow;

    private IdempotentEventFilter(Map<SerializableEventIdentity, Boolean> eventsWindow) {
        this.eventsWindow = eventsWindow;
    }

    /**
     * Test whether an {@link Event} has been seen within a recent events window.
     * @param event The {@link Event} to test.
     * @return true if the {@link Event} has not been seen within the recent events window, false otherwise.
     */
    @Override
    public boolean test(Event event) {
        return eventsWindow.putIfAbsent(SerializableEventIdentity.of(event), true) == null;
    }

}
