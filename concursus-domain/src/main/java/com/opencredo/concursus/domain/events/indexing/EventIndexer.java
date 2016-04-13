package com.opencredo.concursus.domain.events.indexing;

import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventOutChannel;
import com.opencredo.concursus.domain.time.StreamTimestamp;

/**
 * Indexes events by parameter name and value.
 */
@FunctionalInterface
public interface EventIndexer extends EventOutChannel {

    default void accept(Event event) {
        event.getParameters().toMap().forEach((k, v) -> index(event.getAggregateId(), event.getEventTimestamp(), k, v));
    }

    /**
     * Index the event's aggregateId against the given parameterName and parameterValue, at the given timestamp.
     * @param aggregateId The aggregateId to index.
     * @param timestamp The timestamp of the event.
     * @param parameterName The name of the parameter to index against.
     * @param parameterValue The value of the parameter to index against.
     */
    void index(AggregateId aggregateId, StreamTimestamp timestamp, String parameterName, Object parameterValue);
}
