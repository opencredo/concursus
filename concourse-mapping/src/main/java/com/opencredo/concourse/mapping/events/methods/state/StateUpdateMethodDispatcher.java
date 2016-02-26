package com.opencredo.concourse.mapping.events.methods.state;

import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.mapping.reflection.ParameterArgs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class StateUpdateMethodDispatcher implements BiConsumer<Object, Event> {

    static StateUpdateMethodDispatcher of(Class<?> stateClass, String aggregateType, Method method) {
        checkNotNull(stateClass, "stateClass must not be null");
        checkNotNull(method, "method must not be null");
        checkArgument(method.getReturnType().equals(void.class),
                "Method %s with return type %s is not update method for %s", method, method.getReturnType(), stateClass);

        EventType eventType = EventType.of(
                aggregateType,
                StateMethodReflection.getName(method));

        ParameterArgs parameterArgs = ParameterArgs.forMethod(method, 0);
        TupleSchema tupleSchema = parameterArgs.getTupleSchema(eventType.toString());
        TupleKey[] tupleKeys = parameterArgs.getTupleKeys(tupleSchema);

        return new StateUpdateMethodDispatcher(
                method,
                1,
                eventType,
                tupleSchema,
                tupleKeys
        );
    }

    private final Method method;
    private final int causalOrder;
    private final EventType eventType;
    private final TupleSchema tupleSchema;
    private final TupleKey[] tupleKeys;

    public StateUpdateMethodDispatcher(Method method, int causalOrder, EventType eventType, TupleSchema tupleSchema, TupleKey[] tupleKeys) {
        this.method = method;
        this.causalOrder = causalOrder;
        this.eventType = eventType;
        this.tupleSchema = tupleSchema;
        this.tupleKeys = tupleKeys;
    }

    @Override
    public void accept(Object target, Event event) {
        try {
            method.invoke(target, constructArguments(event));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private Object[] constructArguments(Event event) {
        Function<TupleKey, Object> getParameter = event.getParameters()::get;
        return Stream.of(tupleKeys).map(getParameter).toArray();
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
}
