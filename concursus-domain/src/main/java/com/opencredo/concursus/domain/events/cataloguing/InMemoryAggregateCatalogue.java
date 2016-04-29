package com.opencredo.concursus.domain.events.cataloguing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * An in-memory implementation of {@link AggregateCatalogue}, for testing purposes.
 */
public final class InMemoryAggregateCatalogue implements AggregateCatalogue {

    private final ConcurrentMap<String, Set<String>> aggregateIdsByType = new ConcurrentHashMap<>();

    @Override
    public void add(String aggregateType, String aggregateId) {
        aggregateIdsByType.compute(aggregateType, (type, set) -> {
            Set<String> idSet = set == null
                    ? new HashSet<>()
                    : set;
            idSet.add(aggregateId);
            return idSet;
        });
    }

    @Override
    public void remove(String aggregateType, String aggregateId) {
        aggregateIdsByType.compute(aggregateType, (type, set) -> {
            if (set == null) {
                return null;
            }
            set.remove(aggregateId);
            return (set.isEmpty() ? null : set);
        });
    }

    @Override
    public List<String> getAggregateIds(String aggregateType) {
        return aggregateIdsByType.getOrDefault(aggregateType, Collections.emptySet())
                .stream().collect(Collectors.toList());
    }
}
