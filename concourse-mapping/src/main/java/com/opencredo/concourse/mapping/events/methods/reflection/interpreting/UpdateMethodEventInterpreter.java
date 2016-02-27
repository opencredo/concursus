package com.opencredo.concourse.mapping.events.methods.reflection.interpreting;

import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.EventInterpreter;

import java.util.function.Function;
import java.util.stream.Stream;

final class UpdateMethodEventInterpreter implements EventInterpreter {

    static EventInterpreter usingKeys(TupleKey[] tupleKeys) {
        return new UpdateMethodEventInterpreter(tupleKeys);
    }

    private final TupleKey[] tupleKeys;

    private UpdateMethodEventInterpreter(TupleKey[] tupleKeys) {
        this.tupleKeys = tupleKeys;
    }

    @Override
    public Object[] mapEvent(Event event) {
        Function<TupleKey, Object> getParameter = event.getParameters()::get;
        return Stream.of(tupleKeys).map(getParameter).toArray();
    }

}
