package com.opencredo.concourse.domain.events.cataloguing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * An in-memory implementation of {@link AggregateCatalogue}, for testing purposes.
 */
public final class InMemoryAggregateCatalogue implements AggregateCatalogue {

    private final ConcurrentMap<String, Set<UUID>> aggregateIdsByType = new ConcurrentHashMap<>();

    @Override
    public void add(String aggregateType, UUID aggregateId) {
        aggregateIdsByType.compute(aggregateType, (type, set) -> {
            Set<UUID> idSet = set == null
                    ? new HashSet<>()
                    : set;
            idSet.add(aggregateId);
            return idSet;
        });
    }

    @Override
    public void remove(String aggregateType, UUID aggregateId) {
        aggregateIdsByType.compute(aggregateType, (type, set) -> {
            if (set == null) {
                return null;
            }
            set.remove(aggregateId);
            return (set.isEmpty() ? null : set);
        });
    }

    @Override
    public List<UUID> getUuids(String aggregateType) {
        return aggregateIdsByType.getOrDefault(aggregateType, Collections.emptySet())
                .stream().collect(Collectors.toList());
    }
}
