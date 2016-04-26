package com.opencredo.concursus.domain.events.indexing;

import com.opencredo.concursus.data.tuples.TupleKey;
import com.opencredo.concursus.data.tuples.TupleSchema;
import com.opencredo.concursus.data.tuples.TupleSlot;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import org.junit.Test;

import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class InMemoryEventIndexTest {

    private static final TupleSchema eventSchema = TupleSchema.of("test",
            TupleSlot.of("name", String.class),
            TupleSlot.of("age", int.class),
            TupleSlot.of("favouriteColour", String.class));

    private static final TupleKey<String> name = eventSchema.getKey("name", String.class);
    private static final TupleKey<Integer> age = eventSchema.getKey("age", int.class);
    private static final TupleKey<String> favouriteColour = eventSchema.getKey("favouriteColour", String.class);
    public static final VersionedName CREATED = VersionedName.of("created", "0");

    private final InMemoryEventIndex index = InMemoryEventIndex.create();

    private final AggregateId id1 = AggregateId.of("person", "id1");
    private final AggregateId id2 = AggregateId.of("person", "id2");
    @Test
    public void indexesAggregateIdsByPropertyValues() {
        index.accept(Event.of(
                id1,
                StreamTimestamp.now(),
                CREATED,
                eventSchema.make(
                        name.of("Arthur Putey"),
                        age.of(42),
                        favouriteColour.of("orange"))));

        index.accept(Event.of(
                id2,
                StreamTimestamp.now(),
                CREATED,
                eventSchema.make(
                        name.of("Arthur Daley"),
                        age.of(31),
                        favouriteColour.of("orange"))));


        assertThat(index.findAggregates("name", "Arthur Putey"), contains(id1));
        assertThat(index.findAggregates("name", "Arthur Daley"), contains(id2));
        assertThat(index.findAggregates("favouriteColour", "orange"), containsInAnyOrder(id1, id2));
    }

    @Test public void
    newerValuesOverwriteOlderValues() {
        StreamTimestamp now = StreamTimestamp.now();
        StreamTimestamp earlier = now.minus(1, ChronoUnit.MILLIS);
        StreamTimestamp later = now.plus(1, ChronoUnit.MILLIS);

        index.accept(Event.of(
                id1,
                now,
                CREATED,
                eventSchema.make(
                        name.of("Arthur Putey"),
                        age.of(42),
                        favouriteColour.of("orange"))));

        index.accept(Event.of(
                id1,
                earlier,
                CREATED,
                eventSchema.make(
                        name.of("Arthur Daley"),
                        age.of(42),
                        favouriteColour.of("orange"))));

        assertThat(index.findAggregates("name", "Arthur Daley"), hasSize(0));
        assertThat(index.findAggregates("name", "Arthur Putey"), contains(id1));

        index.accept(Event.of(
                id1,
                later,
                CREATED,
                eventSchema.make(
                        name.of("Arthur Mumby"),
                        age.of(42),
                        favouriteColour.of("orange"))));

        assertThat(index.findAggregates("name", "Arthur Putey"), hasSize(0));
        assertThat(index.findAggregates("name", "Arthur Mumby"), contains(id1));
    }
}
