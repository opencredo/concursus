package com.opencredo.concursus.domain.events.state;

import java.time.Instant;
import java.util.*;

/**
 * A repository of state objects.
 * @param <T> The type of the state objects made available by this repository.
 */
public interface StateRepository<T> {

    /**
     * Fetch an object representing the state of the aggregate with this id, at the given moment in time.
     * @param aggregateId The aggregate id to query for.
     * @param upTo The moment in time (exclusive) at which to obtain the aggregate's state.
     * @return The state of the aggregate, if it existed at that moment.
     */
    Optional<T> getState(UUID aggregateId, Instant upTo);

    /**
     * Fetch an object representing the current state of the aggregate with this id.
     * @param aggregateId The aggregate id to query for.
     * @return The state of the aggregate, if it exists.
     */
    default Optional<T> getState(UUID aggregateId) {
        return getState(aggregateId, Instant.MAX);
    }

    /**
     * Fetch a collection of objects representing the state of the aggregates with the requested ids, at the given
     * moment in time.
     * @param aggregateIds The aggregate ids to retrieve states for.
     * @param upTo The moment in time (exclusive) at which to obtain the aggregates' state.
     * @return The aggregates' states, mapped by aggregate id.
     */
    Map<UUID, T> getStates(Collection<UUID> aggregateIds, Instant upTo);

    /**
     * Fetch a collection of objects representing the current state of the aggregates with the requested ids.
     * moment in time.
     * @param aggregateIds The aggregate ids to retrieve states for.
     * @return The aggregates' states, mapped by aggregate id.
     */
    default Map<UUID, T> getStates(Collection<UUID> aggregateIds) {
        return getStates(aggregateIds, Instant.MAX);
    }

    /**
     * Fetch a collection of objects representing the current state of the aggregates with the requested ids.
     * moment in time.
     * @param aggregateIds The aggregate ids to retrieve states for.
     * @return The aggregates' states, mapped by aggregate id.
     */
    default Map<UUID, T> getStates(UUID...aggregateIds) {
        return getStates(Arrays.asList(aggregateIds));
    }
}
