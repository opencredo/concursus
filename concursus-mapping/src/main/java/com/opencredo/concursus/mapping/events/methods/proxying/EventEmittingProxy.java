package com.opencredo.concursus.mapping.events.methods.proxying;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.mapping.events.methods.reflection.EmitterInterfaceInfo;
import com.opencredo.concursus.mapping.events.methods.reflection.EventMethodMapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A proxy that converts method invocations into events
 */
public final class EventEmittingProxy<T> implements InvocationHandler {

    /**
     * Create an EventEmittingProxy proxying the given class, and passing the emitted events to the supplied consumer.
     * @param eventConsumer The Consumer that will be called back with events.
     * @param iface The class to proxy.
     * @param <T> The type of the class to proxy.
     * @return The proxy instance.
     */
    public static <T> T proxying(Consumer<Event> eventConsumer, Class<? extends T> iface) {
        checkNotNull(eventConsumer, "eventConsumer must not be null");

        return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(),
                new Class<?>[] { iface },
                new EventEmittingProxy<>(eventConsumer, EmitterInterfaceInfo.forInterface(iface).getEventMethodMapper())
        ));
    }

    private final Consumer<Event> eventConsumer;
    private final EventMethodMapper eventMethodMapper;

    private EventEmittingProxy(Consumer<Event> eventConsumer, EventMethodMapper eventMethodMapper) {
        this.eventConsumer = eventConsumer;
        this.eventMethodMapper = eventMethodMapper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().isAssignableFrom(getClass())) {
            return method.invoke(this, args);
        }

        eventConsumer.accept(eventMethodMapper.mapMethodCall(method, args));
        return null;
    }
}
