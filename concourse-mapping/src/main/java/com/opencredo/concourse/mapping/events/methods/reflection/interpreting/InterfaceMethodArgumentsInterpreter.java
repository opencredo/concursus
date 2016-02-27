package com.opencredo.concourse.mapping.events.methods.reflection.interpreting;

import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.data.tuples.TupleKeyValue;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.MethodArgumentsInterpreter;

import java.util.UUID;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class InterfaceMethodArgumentsInterpreter implements MethodArgumentsInterpreter {

    static InterfaceMethodArgumentsInterpreter using(EventType eventType, TupleSchema tupleSchema, TupleKey[] tupleKeys) {
        return new InterfaceMethodArgumentsInterpreter(eventType, tupleSchema, tupleKeys);
    }

    private final EventType eventType;
    private final TupleSchema tupleSchema;
    private final TupleKey[] tupleKeys;

    public InterfaceMethodArgumentsInterpreter(EventType eventType, TupleSchema tupleSchema, TupleKey[] tupleKeys) {
        this.eventType = eventType;
        this.tupleSchema = tupleSchema;
        this.tupleKeys = tupleKeys;
    }

    @Override
    public Event mapArguments(Object[] args) {
        checkNotNull(args, "args must not be null");
        checkArgument(args.length == tupleKeys.length + 2,
                "Expected %s args, received %s", tupleKeys.length +2, args.length);

        return eventType.makeEvent((UUID) args[1], (StreamTimestamp) args[0], makeTupleFromArgs(args));
    }

    private Tuple makeTupleFromArgs(Object[] args) {
        return tupleSchema.make(IntStream.range(0, tupleKeys.length)
                .mapToObj(getValueFrom(args))
                .toArray(TupleKeyValue[]::new));
    }

    @SuppressWarnings("unchecked")
    private IntFunction<TupleKeyValue> getValueFrom(Object[] args) {
        return i -> tupleKeys[i].of(args[i + 2]);
    }
}
