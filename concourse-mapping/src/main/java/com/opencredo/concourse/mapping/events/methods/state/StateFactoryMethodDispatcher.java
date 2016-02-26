package com.opencredo.concourse.mapping.events.methods.state;

import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.mapping.reflection.ParameterArgs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class StateFactoryMethodDispatcher<T> implements Function<Event, T> {

    static <T> StateFactoryMethodDispatcher<T> of(Class<? extends T> stateClass, String aggregateType, Method method) {
        checkNotNull(stateClass, "stateClass must not be null");
        checkNotNull(method, "method must not be null");
        checkArgument(method.getParameterTypes()[0].equals(UUID.class),
                "First parameter of method %s is not UUID", method);
        checkArgument(method.getReturnType().equals(stateClass),
                "Method %s is not factory method for %s", method, stateClass);

        EventType eventType = EventType.of(
                aggregateType,
                StateMethodReflection.getName(method));

        ParameterArgs parameterArgs = ParameterArgs.forMethod(method, 1);
        TupleSchema tupleSchema = parameterArgs.getTupleSchema(eventType.toString());
        TupleKey[] tupleKeys = parameterArgs.getTupleKeys(tupleSchema);

        return new StateFactoryMethodDispatcher<>(
                stateClass,
                method,
                eventType,
                tupleSchema,
                tupleKeys
        );
    }

    private final Class<? extends T> stateClass;
    private final Method method;
    private final EventType eventType;
    private final TupleSchema tupleSchema;
    private final TupleKey[] tupleKeys;

    public StateFactoryMethodDispatcher(Class<? extends T> stateClass, Method method, EventType eventType, TupleSchema tupleSchema, TupleKey[] tupleKeys) {
        this.stateClass = stateClass;
        this.method = method;
        this.eventType = eventType;
        this.tupleSchema = tupleSchema;
        this.tupleKeys = tupleKeys;
    }

    @Override
    public T apply(Event event) {
        return invokeMethod(constructArguments(event));
    }

    private Object[] constructArguments(Event event) {
        Object[] args = new Object[tupleKeys.length + 1];
        args[0] = event.getAggregateId().getId();
        IntStream.range(0, tupleKeys.length).forEach(i -> args[i + 1] = event.getParameters().get(tupleKeys[i]));
        return args;
    }

    private T invokeMethod(Object[] args) {
        try {
            return stateClass.cast(method.invoke(null, args));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public EventType getEventType() {
        return eventType;
    }

    public TupleSchema getTupleSchema() {
        return tupleSchema;
    }
}
