package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.data.tuples.*;
import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.mapping.reflection.ParameterArgs;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class EventMethodMapping {

    public static EventMethodMapping forMethod(Method method) {
        checkNotNull(method, "method must not be null");

        Class<?> klass = method.getDeclaringClass();

        final String aggregateType = EventInterfaceReflection.getAggregateType(klass);
        final VersionedName eventName = EventInterfaceReflection.getEventName(method);

        ParameterArgs parameterArgs = ParameterArgs.forMethod(method);
        TupleSchema schema = parameterArgs.getTupleSchema(EventType.of(aggregateType, eventName).toString());
        TupleKey[] tupleKeys = parameterArgs.getTupleKeys(schema);

        return new EventMethodMapping(
                aggregateType,
                eventName,
                schema,
                tupleKeys);
    }

    private final String aggregateType;
    private final VersionedName eventName;
    private final TupleSchema tupleSchema;
    private final TupleKey[] tupleKeys;

    private EventMethodMapping(String aggregateType, VersionedName eventName, TupleSchema tupleSchema, TupleKey[] tupleKeys) {
        this.aggregateType = aggregateType;
        this.eventName = eventName;
        this.tupleSchema = tupleSchema;
        this.tupleKeys = tupleKeys;
    }

    public Event mapArguments(Object[] args) {
        checkNotNull(args, "args must not be null");
        checkArgument(args.length == tupleKeys.length + 2,
                "Expected %s args, received %s", tupleKeys.length +2, args.length);

        return Event.of(
                AggregateId.of(aggregateType, (UUID) args[1]),
                (StreamTimestamp) args[0],
                eventName,
                makeTupleFromArgs(args)
        );
    }

    public EventType getEventType() {
        return EventType.of(aggregateType, eventName);
    }

    public TupleSchema getTupleSchema() {
        return tupleSchema;
    }

    private Tuple makeTupleFromArgs(Object[] args) {
        return tupleSchema.make(IntStream.range(0, tupleKeys.length)
                .mapToObj(getValueFrom(args))
                .toArray(TupleKeyValue[]::new));
    }

    public Object[] mapEvent(Event event) {
        checkNotNull(event, "event must not be null");

        Object[] args = new Object[tupleKeys.length + 2];
        args[0] = event.getEventTimestamp();
        args[1] = event.getAggregateId().getId();

        populateArgsFromTuple(event, args);

        return args;
    }

    public void registerSchema(TupleSchemaRegistry registry) {
        registry.add(tupleSchema);
    }

    private void populateArgsFromTuple(Event event, Object[] args) {
        IntStream.range(0, tupleKeys.length).forEach(i ->
            args[i + 2] = event.getParameters().get(tupleKeys[i]));
    }

    @SuppressWarnings("unchecked")
    private IntFunction<TupleKeyValue> getValueFrom(Object[] args) {
        return i -> tupleKeys[i].of(args[i + 2]);
    }
}
