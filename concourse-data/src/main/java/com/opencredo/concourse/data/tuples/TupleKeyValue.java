package com.opencredo.concourse.data.tuples;

/**
 * A {@link TupleKey} together with the value to be stored in the corresponding {@link TupleSlot}
 */
public final class TupleKeyValue {

    private final TupleKey<?> tupleKey;
    private final Object value;

    TupleKeyValue(TupleKey tupleKey, Object value) {
        this.tupleKey = tupleKey;
        this.value = value;
    }

    boolean belongsToSchema(TupleSchema schema) {
        return tupleKey.belongsToSchema(schema);
    }

    void build(Object[] values) {
        tupleKey.set(values, value);
    }

    public TupleKey<?> getTupleKey() {
        return tupleKey;
    }
}
