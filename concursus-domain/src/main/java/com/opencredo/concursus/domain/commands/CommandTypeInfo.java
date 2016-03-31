package com.opencredo.concursus.domain.commands;

import com.opencredo.concursus.data.tuples.TupleSchema;

import java.lang.reflect.Type;

public final class CommandTypeInfo {

    public static CommandTypeInfo of(TupleSchema tupleSchema, Type returnType) {
        return new CommandTypeInfo(tupleSchema, returnType);
    }

    private final TupleSchema tupleSchema;
    private final Type returnType;

    private CommandTypeInfo(TupleSchema tupleSchema, Type returnType) {
        this.tupleSchema = tupleSchema;
        this.returnType = returnType;
    }

    public TupleSchema getTupleSchema() {
        return tupleSchema;
    }

    public Type getReturnType() {
        return returnType;
    }
}
