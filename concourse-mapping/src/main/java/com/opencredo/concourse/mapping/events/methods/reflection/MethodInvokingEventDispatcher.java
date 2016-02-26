package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.domain.events.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class MethodInvokingEventDispatcher<T> implements EventDispatcher<T> {

    static <T> EventDispatcher<T> dispatching(Method method, EventMethodMapping methodMapping) {
        return new MethodInvokingEventDispatcher<>(method, methodMapping);
    }

    private final Method method;
    private final EventMethodMapping methodMapping;

    private MethodInvokingEventDispatcher(Method method, EventMethodMapping methodMapping) {
        this.method = method;
        this.methodMapping = methodMapping;
    }

    @Override
    public void accept(T target, Event event) {
        try {
            method.invoke(target, methodMapping.mapEvent(event));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
