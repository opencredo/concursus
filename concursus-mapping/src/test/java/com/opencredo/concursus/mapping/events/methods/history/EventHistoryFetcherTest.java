package com.opencredo.concursus.mapping.events.methods.history;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.events.sourcing.EventSource;
import com.opencredo.concursus.domain.events.storage.InMemoryEventStore;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.helper.PersonEvents;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EventHistoryFetcherTest {

    private final InMemoryEventStore eventStore = InMemoryEventStore.empty();
    private final EventSource eventSource = EventSource.retrievingWith(eventStore);

    private final ProxyingEventBus eventBus = ProxyingEventBus.proxying(EventBus.processingWith(EventBatchProcessor.forwardingTo(eventStore)));

    private final Instant timestampStart = Instant.now();
    private final AtomicInteger timestampOffset = new AtomicInteger(0);

    @Test
    public void obtainsEventHistoryInAscendingOrder() {
        String id1 = "id1";
        String id2 = "id2";

        eventBus.dispatch(PersonEvents.class, batch -> {
            batch.createdV2(nextTimestamp(), id1, "Arthur Putey", 41);
            batch.createdV1(nextTimestamp(), id2, "Arthur Dent");
            batch.nameUpdated(nextTimestamp(), id1, "Arthur Daley");
            batch.nameUpdated(nextTimestamp(), id2, "Arthur Mumby");
        });

        Map<String, List<Event>> histories = MappingEventHistoryFetcher.mapping(PersonEvents.class)
                .getHistories(eventSource, Arrays.asList(id1, id2));

        assertThat(histories.get(id1).get(0).getParameters().get("name"),
                equalTo("Arthur Putey"));
        assertThat(histories.get(id1).get(1).getParameters().get("updatedName"),
                equalTo("Arthur Daley"));
        assertThat(histories.get(id2).get(0).getParameters().get("name"),
                equalTo("Arthur Dent"));
        assertThat(histories.get(id2).get(1).getParameters().get("updatedName"),
                equalTo("Arthur Mumby"));
    }

    @Test
    public void causallyOrdersEvents() {
        String id1 = "id1";
        String id2 = "id2";

        eventBus.dispatch(PersonEvents.class, batch -> {
            batch.nameUpdated(nextTimestamp(), id1, "Arthur Daley");
            batch.nameUpdated(nextTimestamp(), id2, "Arthur Mumby");
            batch.createdV1(nextTimestamp(), id2, "Arthur Dent");
            batch.createdV2(nextTimestamp(), id1, "Arthur Putey", 41);
        });

        Map<String, List<Event>> histories = MappingEventHistoryFetcher.mapping(PersonEvents.class)
                .getHistories(eventSource, Arrays.asList(id1, id2));

        assertThat(histories.get(id1).get(0).getParameters().get("name"),
                equalTo("Arthur Putey"));
        assertThat(histories.get(id1).get(1).getParameters().get("updatedName"),
                equalTo("Arthur Daley"));
        assertThat(histories.get(id2).get(0).getParameters().get("name"),
                equalTo("Arthur Dent"));
        assertThat(histories.get(id2).get(1).getParameters().get("updatedName"),
                equalTo("Arthur Mumby"));
    }



    private StreamTimestamp nextTimestamp() {
        return StreamTimestamp.of("test", timestampStart.plusMillis(timestampOffset.getAndIncrement()));
    }

}
