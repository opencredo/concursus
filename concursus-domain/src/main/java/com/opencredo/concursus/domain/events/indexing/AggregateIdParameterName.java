package com.opencredo.concursus.domain.events.indexing;

import com.opencredo.concursus.domain.common.AggregateId;

import java.util.Objects;

final class AggregateIdParameterName {
    static AggregateIdParameterName of(AggregateId aggregateId, String parameterName) {
        return new AggregateIdParameterName(aggregateId, parameterName);
    }

    private final AggregateId aggregateId;
    private final String parameterName;

    private AggregateIdParameterName(AggregateId aggregateId, String parameterName) {
        this.aggregateId = aggregateId;
        this.parameterName = parameterName;
    }

    public AggregateId getAggregateId() {
        return aggregateId;
    }

    public String getParameterName() {
        return parameterName;
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                (o instanceof AggregateIdParameterName
                        && ((AggregateIdParameterName) o).aggregateId.equals(aggregateId)
                        && ((AggregateIdParameterName) o).parameterName.equals(parameterName));
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateId, parameterName);
    }

    @Override
    public String toString() {
        return parameterName + " of " + aggregateId;
    }
}
