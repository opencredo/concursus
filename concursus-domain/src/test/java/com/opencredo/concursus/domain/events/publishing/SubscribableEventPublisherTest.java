package com.opencredo.concursus.domain.events.publishing;

import com.opencredo.concursus.data.tuples.TupleSchema;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.EventType;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class SubscribableEventPublisherTest {

    private final SubscribableEventPublisher unit = new SubscribableEventPublisher();

    @Test
    public void pushesEventsOutToSubscribersByType() {
        List<Event> createdEvents = new ArrayList<>();
        List<Event> updatedEvents = new ArrayList<>();

        unit.subscribe(EventType.of("user", VersionedName.of("created")), createdEvents::add);
        unit.subscribe(EventType.of("group", VersionedName.of("created")), createdEvents::add);
        unit.subscribe(EventType.of("user", VersionedName.of("updated")), updatedEvents::add);

        unit.accept(Event.of(
                AggregateId.of("user", UUID.randomUUID()),
                StreamTimestamp.of("test", Instant.now()),
                VersionedName.of("created"),
                TupleSchema.of("test").makeWith()
        ));

        unit.accept(Event.of(
                AggregateId.of("group", UUID.randomUUID()),
                StreamTimestamp.of("test", Instant.now()),
                VersionedName.of("created"),
                TupleSchema.of("test").makeWith()
        ));

        unit.accept(Event.of(
                AggregateId.of("user", UUID.randomUUID()),
                StreamTimestamp.of("test", Instant.now()),
                VersionedName.of("updated"),
                TupleSchema.of("test").makeWith()
        ));

        assertThat(createdEvents, hasSize(2));
        assertThat(updatedEvents, hasSize(1));
    }
}
