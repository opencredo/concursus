package com.opencredo.concourse.domain.events.cataloguing;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventCharacteristics;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface AggregateCatalogue extends Consumer<Event> {

    void add(String aggregateType, UUID aggregateId);
    void remove(String aggregateType, UUID aggregateId);
    List<UUID> getUuids(String aggregateType);

    @Override
    default void accept(Event event) {
        if (event.hasCharacteristic(EventCharacteristics.IS_INITIAL)) {
            add(event.getAggregateId().getType(), event.getAggregateId().getId());
        }

        if (event.hasCharacteristic(EventCharacteristics.IS_TERMINAL)) {
            remove(event.getAggregateId().getType(), event.getAggregateId().getId());
        }
    }

}
