package com.opencredo.concursus.domain.events.indexing;

import java.util.Objects;

final class ParameterNameAndValue {
    static ParameterNameAndValue of(String parameterName, Object parameterValue) {
        return new ParameterNameAndValue(parameterName, parameterValue);
    }

    private final String parameterName;
    private final Object parameterValue;

    private ParameterNameAndValue(String parameterName, Object parameterValue) {
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                (o instanceof ParameterNameAndValue
                && ((ParameterNameAndValue) o).parameterName.equals(parameterName)
                && ((ParameterNameAndValue) o).parameterValue.equals(parameterValue));
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterName, parameterValue);
    }

    @Override
    public String toString() {
        return parameterName + ":" + parameterValue;
    }
}
