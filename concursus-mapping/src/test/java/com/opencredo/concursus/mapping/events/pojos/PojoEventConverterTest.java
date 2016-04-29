package com.opencredo.concursus.mapping.events.pojos;

import com.opencredo.concursus.data.tuples.TupleSchema;
import com.opencredo.concursus.data.tuples.TupleSlot;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.Name;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

public class PojoEventConverterTest {

    private static final TupleSchema created = TupleSchema.of("created_0",
            TupleSlot.of("sessionId", String.class)
    );

    private static final TupleSchema addedItem = TupleSchema.of("addedItem_0",
            TupleSlot.of("itemId", String.class),
            TupleSlot.of("price", BigDecimal.class),
            TupleSlot.of("quantity", int.class)
    );

    private static final TupleSchema purchasedV2 = TupleSchema.of("purchased_2",
            TupleSlot.of("salesTaxPercent", BigDecimal.class)
    );

    public interface OrderEvent {}
    public interface Created extends OrderEvent {}

    public interface AddedItem extends OrderEvent {
        String getItemId();
        BigDecimal getPrice();
        int getQuantity();
    }

    public interface Purchased extends OrderEvent {}

    @Name(value="purchased", version="2")
    public interface PurchasedV2 extends OrderEvent {
        BigDecimal getSalesTaxPercent();
    }

    @Test
    public void convertsEventsBasedOnAnnotations() {
        PojoEventConverter<OrderEvent> converter = PojoEventConverter.mapping(
                Created.class, AddedItem.class, Purchased.class, PurchasedV2.class);

        Event createdEvent = Event.of(
                AggregateId.of("order", "id1"),
                StreamTimestamp.of("test", Instant.now()),
                VersionedName.of("created", "0"),
                created.makeWith("session1"));

        Event addedItemEvent = Event.of(
                AggregateId.of("order", "id1"),
                StreamTimestamp.of("test", Instant.now()),
                VersionedName.of("addedItem", "0"),
                addedItem.makeWith("itemId1", new BigDecimal("12.5"), 3));

        Event purchasedEvent = Event.of(
                AggregateId.of("order", "id1"),
                StreamTimestamp.of("test", Instant.now()),
                VersionedName.of("purchased", "2"),
                purchasedV2.makeWith(new BigDecimal("20.0")));

        assertThat(converter.apply(createdEvent).getParameters(), instanceOf(Created.class));
        assertThat(converter.apply(addedItemEvent).getParameters(), instanceOf(AddedItem.class));
        assertThat(converter.apply(purchasedEvent).getParameters(), instanceOf(PurchasedV2.class));
    }
}
