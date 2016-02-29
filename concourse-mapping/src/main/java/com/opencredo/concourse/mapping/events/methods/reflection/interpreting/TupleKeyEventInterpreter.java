package com.opencredo.concourse.mapping.events.methods.reflection.interpreting;

import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.domain.events.Event;

import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkNotNull;

final class TupleKeyEventInterpreter implements EventInterpreter {

    static EventInterpreter forUpdateMethod(TupleKey[] tupleKeys) {
        return new TupleKeyEventInterpreter(0, tupleKeys,
                (event, args) -> {});
    }

    static EventInterpreter forFactoryMethod(TupleKey[] tupleKeys) {
        return new TupleKeyEventInterpreter(1, tupleKeys,
                (event, args) -> args[0] = event.getAggregateId().getId());
    }

    static EventInterpreter forInterfaceMethod(TupleKey[] tupleKeys) {
        return new TupleKeyEventInterpreter(2, tupleKeys,
                (event, args) -> {
                    args[0] = event.getEventTimestamp();
                    args[1] = event.getAggregateId().getId();
                });
    }

    private final int offset;
    private final TupleKey[] tupleKeys;
    private final BiConsumer<Event, Object[]> populateNonParameterArgs;

    private TupleKeyEventInterpreter(int offset, TupleKey[] tupleKeys, BiConsumer<Event, Object[]> populateNonParameterArgs) {
        this.offset = offset;
        this.tupleKeys = tupleKeys;
        this.populateNonParameterArgs = populateNonParameterArgs;
    }

    @Override
    public Object[] mapEvent(Event event) {
        checkNotNull(event, "event must not be null");

        Object[] args = new Object[tupleKeys.length + offset];

        populateNonParameterArgs.accept(event, args);
        populateArgsFromTuple(event, args);

        return args;
    }

    private void populateArgsFromTuple(Event event, Object[] args) {
        int keyIndex = 0;
        int argIndex = offset;
        final Tuple parameters = event.getParameters();
        while (keyIndex < tupleKeys.length) {
            args[argIndex++] = parameters.get(tupleKeys[keyIndex++]);
        }
    }
}
