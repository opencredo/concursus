package com.opencredo.concursus.domain.events.indexing;

import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.time.StreamTimestamp;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * An in-memory event index that indexes aggregate ids by most recently-observed event property values.
 */
public final class InMemoryEventIndex implements EventIndexer, EventIndex {

    /**
     * Create a new in-memory event index.
     * @return The constructed {@link InMemoryEventIndex}.
     */
    public static InMemoryEventIndex create() {
        return new InMemoryEventIndex(InMemoryTimestampedTable.create());
    }

    private InMemoryEventIndex(InMemoryTimestampedTable<AggregateIdParameterName, ParameterNameAndValue, StreamTimestamp> valueTable) {
        this.valueTable = valueTable;
    }

    private final InMemoryTimestampedTable<AggregateIdParameterName, ParameterNameAndValue, StreamTimestamp> valueTable;

    @Override
    public Set<AggregateId> findAggregates(String parameterName, Object parameterValue) {
        return valueTable.getIndexed(ParameterNameAndValue.of(parameterName, parameterValue))
                .stream().map(AggregateIdParameterName::getAggregateId).collect(Collectors.toSet());
    }

    @Override
    public void index(AggregateId aggregateId, StreamTimestamp timestamp, String parameterName, Object parameterValue) {
        valueTable.update(
                AggregateIdParameterName.of(aggregateId, parameterName),
                ParameterNameAndValue.of(parameterName, parameterValue),
                timestamp);
    }


}
