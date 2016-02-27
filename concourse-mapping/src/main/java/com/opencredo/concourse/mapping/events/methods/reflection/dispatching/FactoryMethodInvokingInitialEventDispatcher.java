package com.opencredo.concourse.mapping.events.methods.reflection.dispatching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.EventInterpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class FactoryMethodInvokingInitialEventDispatcher<T> implements InitialEventDispatcher<T> {

    public static <T> FactoryMethodInvokingInitialEventDispatcher<T> dispatching(Class<? extends T> targetClass, Method method, EventInterpreter eventInterpreter) {
        return new FactoryMethodInvokingInitialEventDispatcher<T>(targetClass, method, eventInterpreter);
    }

    private final Class<? extends T> targetClass;
    private final Method method;
    private final EventInterpreter eventInterpreter;

    private FactoryMethodInvokingInitialEventDispatcher(Class<? extends T> targetClass, Method method, EventInterpreter eventInterpreter) {
        this.targetClass = targetClass;
        this.method = method;
        this.eventInterpreter = eventInterpreter;
    }

    @Override
    public T apply(Event event) {
        try {
            return targetClass.cast(method.invoke(null, eventInterpreter.mapEvent(event)));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
