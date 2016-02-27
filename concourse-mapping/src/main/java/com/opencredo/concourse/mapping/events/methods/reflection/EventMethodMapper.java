package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.MethodArgumentsInterpreter;

import java.lang.reflect.Method;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * Given a method and an array of arguments, construct the corresponding {@link Event}
 */
public final class EventMethodMapper {

    static EventMethodMapper mappingWith(Map<Method, ? extends MethodArgumentsInterpreter> argumentsInterpreters) {
        return new EventMethodMapper(argumentsInterpreters);
    }

    private final Map<Method, ? extends MethodArgumentsInterpreter> argumentsInterpreters;

    private EventMethodMapper(Map<Method, ? extends MethodArgumentsInterpreter> argumentsInterpreters) {
        this.argumentsInterpreters = argumentsInterpreters;
    }

    /**
     * Map a method call to an {@link Event}.
     * @param method The method that was called
     * @param args The arguments that were passed to the method
     * @return The constructed {@link Event}
     */
    public Event mapMethodCall(Method method, Object[] args) {
        MethodArgumentsInterpreter mapping = argumentsInterpreters.get(method);
        checkState(mapping != null, "No mapping found for method %s", method);

        return mapping.mapArguments(args);
    }
}
