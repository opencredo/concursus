package com.opencredo.concursus.domain.json.events.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventsInChannel;
import com.opencredo.concursus.domain.events.channels.EventsOutChannel;
import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concursus.mapping.events.methods.reflection.EmitterInterfaceInfo;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class JsonEventOutChannelTest {

    @HandlesEventsFor("test")
    public interface TestEvents {
        void created(StreamTimestamp ts, UUID id, String name);
    }

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final List<Event> receivedEvents = new ArrayList<>();
    private final EventsOutChannel receiverIn = receivedEvents::addAll;

    private final JsonEventsInChannel jsonIn = JsonEventsInChannel.using(
            objectMapper,
            EmitterInterfaceInfo.forInterface(TestEvents.class).getEventTypeMatcher(),
            receiverIn);

    private final EventsInChannel<String> loggingIn = json -> {
        System.out.println(json);
        jsonIn.accept(json);
    };

    private final EventsOutChannel jsonOut = JsonEventsOutChannel.using(objectMapper, loggingIn);

    private final EventBus eventBus = EventBus.processingWith(
            EventBatchProcessor.loggingWith(
                    EventLog.loggingTo(jsonOut)));

    private final ProxyingEventBus proxyingEventBus = ProxyingEventBus.proxying(eventBus);

    @Test
    public void transmitsEventsOverJsonTransport() {
        StreamTimestamp ts = StreamTimestamp.of("test", Instant.now());
        UUID id = UUID.randomUUID();

        proxyingEventBus.dispatch(TestEvents.class, e -> e.created(ts, id, "Arthur Mumby"));

        assertThat(receivedEvents.get(0).getAggregateId(), equalTo(AggregateId.of("test", id)));
    }
}
