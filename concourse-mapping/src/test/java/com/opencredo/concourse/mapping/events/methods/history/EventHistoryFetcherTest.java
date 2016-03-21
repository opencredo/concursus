package com.opencredo.concourse.mapping.events.methods.history;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.batching.ProcessingEventBatch;
import com.opencredo.concourse.domain.events.processing.EventBatchProcessor;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.storing.InMemoryEventStore;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.events.methods.helper.PersonEvents;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EventHistoryFetcherTest {

    private final InMemoryEventStore eventStore = InMemoryEventStore.empty();
    private final EventSource eventSource = EventSource.retrievingWith(eventStore);

    private final ProxyingEventBus eventBus = ProxyingEventBus.proxying(() -> ProcessingEventBatch.processingWith(EventBatchProcessor.forwardingTo(eventStore)));

    private final Instant timestampStart = Instant.now();
    private final AtomicInteger timestampOffset = new AtomicInteger(0);

    @Test
    public void obtainsEventHistoryInAscendingOrder() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        eventBus.dispatch(PersonEvents.class, batch -> {
            batch.createdV2(nextTimestamp(), id1, "Arthur Putey", 41);
            batch.createdV1(nextTimestamp(), id2, "Arthur Dent");
            batch.nameUpdated(nextTimestamp(), id1, "Arthur Daley");
            batch.nameUpdated(nextTimestamp(), id2, "Arthur Mumby");
        });

        Map<UUID, List<Event>> histories = EventHistoryFetcher.of(PersonEvents.class)
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
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        eventBus.dispatch(PersonEvents.class, batch -> {
            batch.nameUpdated(nextTimestamp(), id1, "Arthur Daley");
            batch.nameUpdated(nextTimestamp(), id2, "Arthur Mumby");
            batch.createdV1(nextTimestamp(), id2, "Arthur Dent");
            batch.createdV2(nextTimestamp(), id1, "Arthur Putey", 41);
        });

        Map<UUID, List<Event>> histories = EventHistoryFetcher.of(PersonEvents.class)
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
