package com.opencredo.concursus.hazelcast.events;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.opencredo.concursus.domain.events.channels.EventOutChannel;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.events.methods.dispatching.DispatchingEventOutChannel;
import com.opencredo.concursus.mapping.events.methods.proxying.EventEmittingProxy;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

public class IdempotentEventFilterTest {

    private HazelcastInstance hazelcastInstance;

    @Before
    public void setupHazelcast() {
        hazelcastInstance = Hazelcast.newHazelcastInstance(
                IdempotentEventFilter.configureCache("eventWindow", 1, new Config()));
    }

    @After
    public void teardownHazelcast() {
        hazelcastInstance.shutdown();
    }

    @HandlesEventsFor("test")
    public interface TestEvents {
        void testEvent(StreamTimestamp ts, String id, String value);
    }

    @Test
    public void discardsDuplicatesWithinEventWindow() throws InterruptedException, ExecutionException {
        TestEvents eventHandler = mock(TestEvents.class);

        EventOutChannel filteredChannel = DispatchingEventOutChannel
                .toHandler(TestEvents.class, eventHandler)
                .filter(IdempotentEventFilter.using(hazelcastInstance, "eventWindow"));

        TestEvents proxy = EventEmittingProxy.proxying(filteredChannel, TestEvents.class);

        final StreamTimestamp timestamp = StreamTimestamp.now();
        final String aggregateId = "id1";

        proxy.testEvent(timestamp, aggregateId, "foo");
        proxy.testEvent(timestamp, aggregateId, "bar");

        verify(eventHandler).testEvent(timestamp, aggregateId, "foo");
        verify(eventHandler, never()).testEvent(timestamp, aggregateId, "bar");
    }

    @Ignore("flickering")
    @Test
    public void allowsDuplicatesOutsideEventWindow() throws InterruptedException, ExecutionException {
        TestEvents eventHandler = mock(TestEvents.class);

        EventOutChannel filteredChannel = DispatchingEventOutChannel
                .toHandler(TestEvents.class, eventHandler)
                .filter(IdempotentEventFilter.using(hazelcastInstance, "eventWindow"));

        TestEvents proxy = EventEmittingProxy.proxying(filteredChannel, TestEvents.class);

        final StreamTimestamp timestamp = StreamTimestamp.now();
        final String aggregateId = "id";

        proxy.testEvent(timestamp, aggregateId, "foo");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        proxy.testEvent(timestamp, aggregateId, "bar");

        verify(eventHandler).testEvent(timestamp, aggregateId, "foo");
        verify(eventHandler).testEvent(timestamp, aggregateId, "bar");
    }
}
