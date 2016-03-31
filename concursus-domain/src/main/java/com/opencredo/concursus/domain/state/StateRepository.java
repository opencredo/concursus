package com.opencredo.concursus.domain.state;

import java.time.Instant;
import java.util.*;

public interface StateRepository<T> {

    Optional<T> getState(UUID aggregateId, Instant upTo);

    default Optional<T> getState(UUID aggregateId) {
        return getState(aggregateId, Instant.MAX);
    }

    Map<UUID, T> getStates(Collection<UUID> aggregateIds, Instant upTo);

    default Map<UUID, T> getStates(Collection<UUID> aggregateIds) {
        return getStates(aggregateIds, Instant.MAX);
    }

    default Map<UUID, T> getStates(UUID...aggregateIds) {
        return getStates(Arrays.asList(aggregateIds));
    }
}
