package com.opencredo.concourse.mapping.events.methods.reflection.interpreting;

import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.EventInterpreter;

import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;

final class InterfaceEventInterpreter implements EventInterpreter {

    static InterfaceEventInterpreter usingKeys(TupleKey[] tupleKeys) {
        return new InterfaceEventInterpreter(tupleKeys);
    }

    private final TupleKey[] tupleKeys;

    private InterfaceEventInterpreter(TupleKey[] tupleKeys) {
        this.tupleKeys = tupleKeys;
    }

    @Override
    public Object[] mapEvent(Event event) {
        checkNotNull(event, "event must not be null");

        Object[] args = new Object[tupleKeys.length + 2];
        args[0] = event.getEventTimestamp();
        args[1] = event.getAggregateId().getId();

        populateArgsFromTuple(event, args);

        return args;
    }

    private void populateArgsFromTuple(Event event, Object[] args) {
        IntStream.range(0, tupleKeys.length).forEach(i ->
            args[i + 2] = event.getParameters().get(tupleKeys[i]));
    }

}
