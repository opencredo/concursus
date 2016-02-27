package com.opencredo.concourse.mapping.events.methods.reflection.interpreting;

import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.mapping.annotations.*;
import com.opencredo.concourse.mapping.events.methods.ordering.CausalOrdering;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.EventInterpreter;
import com.opencredo.concourse.mapping.reflection.ParameterArgs;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class EventInterpreters {

    private EventInterpreters() {
    }

    public static <T> TypeMappingEventInterpreter forFactoryMethod(Class<? extends T> stateClass, String aggregateType, Method factoryMethod) {
        checkNotNull(stateClass, "stateClass must not be null");
        checkNotNull(factoryMethod, "method must not be null");
        checkArgument(factoryMethod.getParameterTypes()[0].equals(UUID.class),
                "First parameter of method %s is not UUID", factoryMethod);
        checkArgument(factoryMethod.getReturnType().equals(stateClass),
                "Method %s is not factory method for %s", factoryMethod, stateClass);

        return makeEventInterpreter(
                getEventType(aggregateType, factoryMethod),
                FactoryMethodEventInterpreter::usingKeys,
                ParameterArgs.forMethod(factoryMethod, 1),
                CausalOrdering.INITIAL);
    }

    public static <T> TypeMappingEventInterpreter forUpdateMethod(Class<? extends T> stateClass, String aggregateType, Method method) {
        checkNotNull(stateClass, "stateClass must not be null");
        checkNotNull(method, "method must not be null");
        checkArgument(method.getReturnType().equals(void.class),
                "Method %s is not update method for %s", method, stateClass);

        return makeEventInterpreter(
                getEventType(aggregateType, method),
                UpdateMethodEventInterpreter::usingKeys,
                ParameterArgs.forMethod(method, 0),
                getOrdering(method));
    }

    private static int getOrdering(Method method) {
        if (method.isAnnotationPresent(Initial.class)) {
            return CausalOrdering.INITIAL;
        }

        if (method.isAnnotationPresent(Terminal.class)) {
            return CausalOrdering.TERMINAL;
        }

        if (method.isAnnotationPresent(Ordered.class)) {
            return method.getAnnotation(Ordered.class).value();
        }

        return CausalOrdering.PRE_TERMINAL;
    }

    public static InterfaceMethodMapping forInterfaceMethod(Method method, String aggregateType) {
        checkNotNull(method, "method must not be null");
        checkNotNull(aggregateType, "aggregateType must not be null");

        EventType eventType = getEventType(aggregateType, method);

        ParameterArgs parameterArgs = ParameterArgs.forMethod(method, 2);
        TupleSchema schema = parameterArgs.getTupleSchema(eventType.toString());
        TupleKey[] tupleKeys = parameterArgs.getTupleKeys(schema);

        InterfaceMethodArgumentsInterpreter methodArgumentsInterpreter = InterfaceMethodArgumentsInterpreter.using(
                eventType, schema, tupleKeys);
        EventInterpreter eventInterpreter = InterfaceEventInterpreter.usingKeys(tupleKeys);

        return InterfaceMethodMapping.using(
                methodArgumentsInterpreter,
                TypeMappingEventInterpreter.create(
                        eventInterpreter,
                        eventType,
                        schema,
                        getOrdering(method)));
    }

    private static TypeMappingEventInterpreter makeEventInterpreter(EventType eventType, Function<TupleKey[], EventInterpreter> factory, ParameterArgs parameterArgs, int causalOrder) {
        TupleSchema tupleSchema = parameterArgs.getTupleSchema(eventType.toString());
        TupleKey[] tupleKeys = parameterArgs.getTupleKeys(tupleSchema);
        return TypeMappingEventInterpreter.create(factory.apply(tupleKeys), eventType, tupleSchema, causalOrder);
    }

    private static EventType getEventType(String aggregateType, Method method) {
        return EventType.of(aggregateType, getEventName(method));
    }

    private static VersionedName getEventName(Method method) {
        if (method.isAnnotationPresent(HandlesEvent.class)) {
            return getEventName(method.getAnnotation(HandlesEvent.class), method.getName());
        }
        if (method.isAnnotationPresent(Name.class)) {
            return getEventName(method.getAnnotation(Name.class));
        }
        return VersionedName.of(method.getName(), "0");
    }

    private static VersionedName getEventName(HandlesEvent handlesEvent, String methodName) {
        return VersionedName.of(
                handlesEvent.value().isEmpty() ? methodName : handlesEvent.value(),
                handlesEvent.version()
        );
    }

    private static VersionedName getEventName(Name name) {
        return VersionedName.of(name.value(), name.version());
    }
}
