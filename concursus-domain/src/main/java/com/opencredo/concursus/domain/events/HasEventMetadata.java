package com.opencredo.concursus.domain.events;

import java.util.Optional;
import java.util.UUID;

public interface HasEventMetadata extends EventMetadata {

    EventMetadata getMetadata();

    /**
     * Get the {@link EventType} of this event.
     * @return The {@link EventType} of this event.
     */
    default EventType getType() {
        return getMetadata().getType();
    }

    /**
     * Get the {@link EventIdentity} of this event.
     * @return The {@link EventIdentity} of this event.
     */
    default EventIdentity getIdentity() {
        return getMetadata().getIdentity();
    }

    /**
     * Get the processing ID of this event (if it has been processed).
     * @return An {@link Optional} containing the event's processing {@link UUID} if it has been processed, or
     * {@link Optional}::empty if it has not.
     */
    default Optional<UUID> getProcessingId() {
        return getMetadata().getProcessingId();
    }

}
