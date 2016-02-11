package com.opencredo.concourse.mapping.methods;

import com.google.common.base.Preconditions;
import com.opencredo.concourse.domain.events.Event;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A proxy that converts method invocations into events
 */
public final class EventEmittingProxy implements InvocationHandler {

    /**
     * Create an EventEmittingProxy proxying the given class, and passing the emitted events to the supplied consumer.
     * @param eventConsumer The Consumer that will be called back with events.
     * @param klass The class to proxy.
     * @param <T> The type of the class to proxy.
     * @return The proxy instance.
     */
    public static <T> T proxying(Consumer<Event> eventConsumer, Class<T> klass) {
        return klass.cast(Proxy.newProxyInstance(klass.getClassLoader(),
                new Class<?>[] { klass },
                new EventEmittingProxy(eventConsumer, EventInterfaceReflection.getEventMappers(klass))
        ));
    }

    private final Consumer<Event> eventConsumer;
    private final Map<Method, MethodMapping> eventMappers;

    private EventEmittingProxy(Consumer<Event> eventConsumer, Map<Method, MethodMapping> eventMappers) {
        this.eventConsumer = eventConsumer;
        this.eventMappers = eventMappers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().isAssignableFrom(getClass())) {
            return method.invoke(this, args);
        }
        MethodMapping mapper = eventMappers.get(method);
        Preconditions.checkState(mapper != null, "No mapper found for method %s", method);

        eventConsumer.accept(mapper.mapArguments(args));
        return null;
    }
}
