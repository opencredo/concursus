package com.opencredo.concourse.mapping.events.methods.reflection.dispatching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.EventInterpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class MethodInvokingEventDispatcher<T> implements EventDispatcher<T> {

    public static <T> EventDispatcher<T> dispatching(Method method, EventInterpreter eventInterpreter) {
        return new MethodInvokingEventDispatcher<>(method, eventInterpreter);
    }

    private final Method method;
    private final EventInterpreter eventInterpreter;

    private MethodInvokingEventDispatcher(Method method, EventInterpreter eventInterpreter) {
        this.method = method;
        this.eventInterpreter = eventInterpreter;
    }

    @Override
    public void accept(T target, Event event) {
        try {
            method.invoke(target, eventInterpreter.mapEvent(event));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
