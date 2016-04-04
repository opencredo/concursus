package com.opencredo.concursus.domain.commands;

import com.opencredo.concursus.data.tuples.TupleSchema;

import java.lang.reflect.Type;

/**
 * Parameter type information for a {@link Command}
 */
public final class CommandTypeInfo {

    /**
     * Create a {@link CommandTypeInfo} instance with the supplied {@link TupleSchema} and return {@link Type}.
     * @param tupleSchema The {@link TupleSchema} for the {@link Command}'s parameters.
     * @param returnType The {@link Type} of the {@link Command}'s return value.
     * @return The constructed {@link CommandTypeInfo}.
     */
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
