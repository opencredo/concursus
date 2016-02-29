package com.opencredo.concourse.mapping.events.methods.reflection.interpreting;

import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.data.tuples.TupleKeyValue;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.events.methods.ordering.CausalOrdering;

import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toMap;

public final class EventMethodInfo {

    public static EventTypeMatcher makeEventTypeMatcher(Collection<? extends EventMethodInfo> typeMappings) {
        return EventTypeMatcher.matchingAgainst(typeMappings.stream().collect(toMap(EventMethodInfo::getEventType, EventMethodInfo::getTupleSchema)));
    }

    public static Comparator<Event> makeCausalOrdering(Collection<? extends EventMethodInfo> typeMappings) {
        return CausalOrdering.onEventTypes(typeMappings.stream().collect(toMap(EventMethodInfo::getEventType, EventMethodInfo::getCausalOrder)));
    }

    private final EventType eventType;
    private final TupleSchema tupleSchema;
    private final TupleKey[] tupleKeys;
    private final int causalOrder;
    private final EventMethodType eventMethodType;

    public EventMethodInfo(EventType eventType, TupleSchema tupleSchema, TupleKey[] tupleKeys, int causalOrder, EventMethodType eventMethodType) {
        this.eventType = eventType;
        this.tupleSchema = tupleSchema;
        this.tupleKeys = tupleKeys;
        this.causalOrder = causalOrder;
        this.eventMethodType = eventMethodType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public TupleSchema getTupleSchema() {
        return tupleSchema;
    }

    public int getCausalOrder() {
        return causalOrder;
    }

    public Object[] mapEvent(Event event) {
        return eventMethodType.apply(event, tupleKeys);
    }

    public Event mapArguments(Object[] args) {
        checkNotNull(args, "args must not be null");
        checkArgument(args.length == tupleKeys.length + 2,
                "Expected %s args, received %s", tupleKeys.length +2, args.length);
        checkArgument(args[1] instanceof UUID, "second argument %s is not a UUID", args[1]);
        checkArgument(args[0] instanceof StreamTimestamp, "first argument %s is not a StreamTimestamp", args[0]);

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
