package com.opencredo.concursus.domain.events.cataloguing;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.EventCharacteristics;
import com.opencredo.concursus.domain.events.channels.EventOutChannel;

import java.util.List;
import java.util.UUID;

/**
 * A catalogue of "active" aggregates, i.e. those that have received an initial event, but have not yet received a terminal event.
 */
public interface AggregateCatalogue extends EventOutChannel {

    /**
     * Add an aggregate to the catalogue.
     * @param aggregateType The type of the aggregate.
     * @param aggregateId The id of the aggregate.
     */
    void add(String aggregateType, UUID aggregateId);

    /**
     * Remove an aggregate from the catalogue.
     * @param aggregateType The type of the aggregate.
     * @param aggregateId The id of the aggregate;
     */
    void remove(String aggregateType, UUID aggregateId);

    /**
     * Obtain a {@link List} of all the aggregate Ids in the catalogue for the given type.
     * @param aggregateType The aggregate type to retrieve ids for.
     * @return The {@link List} of retrieved ids.
     */
    List<UUID> getUuids(String aggregateType);

    /**
     * If the received {@link Event} is an initial event, add the aggregate to the catalogue. If it is a terminal event, remove the aggregate from the catalogue.
     * @param event The {@link Event} to observe.
     */
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
