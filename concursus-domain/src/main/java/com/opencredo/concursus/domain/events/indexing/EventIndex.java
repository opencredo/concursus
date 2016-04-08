package com.opencredo.concursus.domain.events.indexing;

import com.opencredo.concursus.domain.common.AggregateId;

import java.util.Set;

/**
 * Finds aggregate ids by parameter name/value pairs.
 */
@FunctionalInterface
public interface EventIndex {

    /**
     * Find all the aggregates for which the most recently-observed event parameter of the given name had the given value.
     * @param parameterName The parameter name to search for.
     * @param parameterValue The parameter value to search for.
     * @return All matching aggregate ids.
     */
    Set<AggregateId> findAggregates(String parameterName, Object parameterValue);

}
