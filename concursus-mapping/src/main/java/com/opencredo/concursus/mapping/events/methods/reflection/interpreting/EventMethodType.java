package com.opencredo.concursus.mapping.events.methods.reflection.interpreting;

import com.opencredo.concursus.data.tuples.Tuple;
import com.opencredo.concursus.data.tuples.TupleKey;
import com.opencredo.concursus.data.tuples.TupleSchema;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.EventType;
import com.opencredo.concursus.mapping.reflection.MethodSelectors;
import com.opencredo.concursus.mapping.reflection.ParameterArgs;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toMap;

public enum EventMethodType implements BiFunction<Event, TupleKey[], Object[]> {

    EMITTER(2, MethodSelectors.isEventEmittingMethod, (event, args) -> {
        args[0] = event.getEventTimestamp();
        args[1] = event.getAggregateId().getId();
    }),

    FACTORY(1, MethodSelectors.isFactoryMethod, (event, args) -> args[0] = event.getAggregateId().getId()),
    UPDATER(0, MethodSelectors.isUpdateMethod, (event, args) -> {});

    private final int offset;
    private final Predicate<Method> methodMatcher;
    private final BiConsumer<Event, Object[]> nonParameterArgsPopulator;

    EventMethodType(int offset, Predicate<Method> methodMatcher, BiConsumer<Event, Object[]> nonParameterArgsPopulator) {
        this.offset = offset;
        this.methodMatcher = methodMatcher;
        this.nonParameterArgsPopulator = nonParameterArgsPopulator;
    }

    public Map<Method, EventMethodMapping> getEventMethodInfo(String aggregateType, Class<?> eventClass) {
        checkNotNull(aggregateType, "aggregateType must not be null");
        checkNotNull(eventClass, "eventClass must not be null");

        return Stream.of(eventClass.getMethods())
                .filter(methodMatcher)
                .distinct()
                .collect(toMap(Function.identity(), method -> getMethodInfo(aggregateType, method)));
    }

    @Override
    public Object[] apply(Event event, TupleKey[] tupleKeys) {
        checkNotNull(event, "event must not be null");

        Object[] args = new Object[tupleKeys.length + offset];

        nonParameterArgsPopulator.accept(event, args);
        populateArgsFromTuple(event, tupleKeys, args);

        return args;
    }

    private void populateArgsFromTuple(Event event, TupleKey[] tupleKeys, Object[] args) {
        int keyIndex = 0;
        int argIndex = offset;
        final Tuple parameters = event.getParameters();
        while (keyIndex < tupleKeys.length) {
            args[argIndex++] = parameters.get(tupleKeys[keyIndex++]);
        }
    }

    private EventMethodMapping getMethodInfo(String aggregateType, Method method) {
        EventType eventType = EventMethodReflection.getEventType(aggregateType, method);

        ParameterArgs parameterArgs = ParameterArgs.forMethod(method, offset);
        TupleSchema tupleSchema = parameterArgs.getTupleSchema(eventType.toString());
        TupleKey[] tupleKeys = parameterArgs.getTupleKeys(tupleSchema);

        return new EventMethodMapping(
                eventType,
                tupleSchema,
                tupleKeys,
                EventMethodReflection.getOrdering(method),
                this);
    }

}
