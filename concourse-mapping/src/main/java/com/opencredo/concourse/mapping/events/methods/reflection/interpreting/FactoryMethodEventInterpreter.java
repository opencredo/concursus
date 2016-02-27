package com.opencredo.concourse.mapping.events.methods.reflection.interpreting;

import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.EventInterpreter;

import java.util.stream.IntStream;

final class FactoryMethodEventInterpreter implements EventInterpreter {

    static <T> FactoryMethodEventInterpreter usingKeys(TupleKey[] tupleKeys) {
        return new FactoryMethodEventInterpreter(tupleKeys);
    }

    private final TupleKey[] tupleKeys;

    public FactoryMethodEventInterpreter(TupleKey[] tupleKeys) {
        this.tupleKeys = tupleKeys;
    }

    @Override
    public Object[] mapEvent(Event event) {
        Object[] args = new Object[tupleKeys.length + 1];
        args[0] = event.getAggregateId().getId();
        IntStream.range(0, tupleKeys.length).forEach(i -> args[i + 1] = event.getParameters().get(tupleKeys[i]));
        return args;
    }
}
