package com.opencredo.concursus.domain.events;

import com.opencredo.concursus.data.tuples.Tuple;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.time.StreamTimestamp;

import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event in the system.
 */
public final class Event implements EventRepresentation<Tuple> {

    /**
     * Create a processed {@link Event} with the supplied properties.
     * @param aggregateId The {@link AggregateId} of the aggregate to which the event occurred.
     * @param eventTimestamp The {@link StreamTimestamp} representing the time at which the event occurred.
     * @param processingId The processing {@link UUID} of the event (should be present only if the event has actually
     *                     been processed). This must be a type 1 UUID, as it encodes the event's processing timestamp.
     * @param eventName The {@link VersionedName} of the event.
     * @param parameters The event's parameters.
     * @param characteristics The event's characteristics, e.g. whether it is an initial or terminal event.
     * @return The constructed {@link Event}.
     */
    public static Event of(AggregateId aggregateId, StreamTimestamp eventTimestamp, UUID processingId, VersionedName eventName, Tuple parameters, int...characteristics) {
        return of(
                EventMetadata.of(
                        EventType.of(aggregateId.getType(), eventName, characteristics),
                        EventIdentity.of(aggregateId, eventTimestamp),
                        processingId),
                parameters);
    }

    /**
     * Create an unprocessed {@link Event} with the supplied properties.
     * @param aggregateId The {@link AggregateId} of the aggregate to which the event occurred.
     * @param eventTimestamp The {@link StreamTimestamp} representing the time at which the event occurred.
     * @param eventName The {@link VersionedName} of the event.
     * @param parameters The event's parameters.
     * @param characteristics The event's characteristics, e.g. whether it is an initial or terminal event.
     * @return The constructed {@link Event}.
     */
    public static Event of(AggregateId aggregateId, StreamTimestamp eventTimestamp, VersionedName eventName, Tuple parameters, int...characteristics) {
        return of(
                EventMetadata.of(
                        EventType.of(aggregateId.getType(), eventName, characteristics),
                        EventIdentity.of(aggregateId, eventTimestamp)),
                parameters);
    }

    /**
     * Create an {@link Event} with the supplied metadata and parameters.
     * @param eventMetadata The {@link EventMetadata} of the event.
     * @param parameters The {@link Tuple} of event parameters.
     * @return The constructed {@link Event}.
     */
    public static Event of(EventMetadata eventMetadata, Tuple parameters) {
        checkNotNull(eventMetadata, "eventMetadata must not be null");
        checkNotNull(parameters, "parameters must not be null");

        return new Event(eventMetadata, parameters);
    }

    private final EventMetadata eventMetadata;
    private final Tuple parameters;

    private Event(EventMetadata eventMetadata, Tuple parameters) {
        this.eventMetadata = eventMetadata;
        this.parameters = parameters;
    }

    /**
     * Make a copy of this {@link Event} with the processing Id set to the supplied processing id.
     * @param processingId The processing {@link UUID} of the event. This must be a type 1 UUID, as it encodes the
     *                     event's processing timestamp.
     * @return The updated {@link Event}.
     */
    public Event processed(UUID processingId) {
        checkNotNull(processingId, "processingId must not be null");
        checkArgument(processingId.version() == 1, "processingId must be type 1 UUID");

        return new Event(eventMetadata.processed(processingId), parameters);
    }
    /**
     * Get the event's parameters.
     * @return The event's parameters, encoded as a {@link Tuple}.
     */
    public Tuple getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Event && equals(Event.class.cast(o)));
    }

    private boolean equals(Event o) {
        return eventMetadata.equals(o.eventMetadata)
                && parameters.equals(o.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventMetadata, parameters);
    }

    @Override
    public String toString() {
        return getProcessingTime().map(processingTime ->
                String.format("%s\nwith %s\nprocessed at %s",
                        eventMetadata, parameters, processingTime))
                .orElseGet(() -> String.format("%s\nwith %s",
                        eventMetadata, parameters));
    }

    @Override
    public EventMetadata getMetadata() {
        return eventMetadata;
    }

    @Override
    public Tuple getData() {
        return parameters;
    }
}
