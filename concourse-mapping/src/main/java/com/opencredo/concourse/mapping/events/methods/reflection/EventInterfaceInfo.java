package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.domain.events.binding.EventTypeBinding;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.EventDispatchers;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.MultiTypeEventDispatcher;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.EventInterpreters;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.InterfaceMethodMapping;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.TypeMapping;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The source of all reflection information about an event interface.
 * @param <T> The type of the event interface.
 */
public final class EventInterfaceInfo<T> {

    private static final ConcurrentMap<Class<?>, EventInterfaceInfo<?>> cache = new ConcurrentHashMap<>();

    /**
     * Get event interface information from the supplied interface. This method is cached.
     * @param iface The interface to get information for.
     * @param <T> The type of the interface.
     * @return The EventInterfaceInfo for the supplied interface.
     */
    @SuppressWarnings("unchecked")
    public static <T> EventInterfaceInfo<T> forInterface(Class<? extends T> iface) {
        return (EventInterfaceInfo<T>) cache.computeIfAbsent(iface, EventInterfaceInfo::forInterfaceUncached);
    }

    private static <T> EventInterfaceInfo<T> forInterfaceUncached(Class<? extends T> iface) {
        checkNotNull(iface, "iface must not be null");
        checkArgument(iface.isInterface(), "iface must be interface");
        checkArgument(iface.isAnnotationPresent(HandlesEventsFor.class),
                "Interface %s is not annotated with @HandlesEventsFor", iface);

        String aggregateType = iface.getAnnotation(HandlesEventsFor.class).value();
        Map<Method, InterfaceMethodMapping> eventMappers = getEventMappers(iface, aggregateType);

        return new EventInterfaceInfo<>(
                EventTypeBinding.of(aggregateType, TypeMapping.makeEventTypeMatcher(eventMappers.values())),
                EventMethodMapper.mappingWith(eventMappers),
                EventDispatchers.dispatchingEventsByType(eventMappers),
                TypeMapping.makeCausalOrdering(eventMappers.values()));
    }

    private static Map<Method, InterfaceMethodMapping> getEventMappers(Class<?> iface, String aggregateType) {
        return MethodSelectors.interpretMethods(
                iface,
                MethodSelectors.isEventEmittingMethod,
                method -> EventInterpreters.forInterfaceMethod(method, aggregateType));
    }

    private final EventTypeBinding eventTypeBinding;
    private final EventMethodMapper eventMethodMapper;
    private final MultiTypeEventDispatcher<T> eventDispatcher;
    private final Comparator<Event> causalOrderComparator;

    private EventInterfaceInfo(EventTypeBinding eventTypeBinding, EventMethodMapper eventMethodMapper, MultiTypeEventDispatcher<T> eventDispatcher, Comparator<Event> causalOrderComparator) {
        this.eventTypeBinding = eventTypeBinding;
        this.eventMethodMapper = eventMethodMapper;
        this.eventDispatcher = eventDispatcher;
        this.causalOrderComparator = causalOrderComparator;
    }

    /**
     * Get a {@link Comparator} that can be used to sort events in causal order, based on annotations on the interface.
     * @return The comparator.
     */
    public Comparator<Event> getCausalOrderComparator() {
        return causalOrderComparator;
    }

    /**
     * Get {@link EventTypeBinding} for the interface.
     * @return
     */
    public EventTypeBinding getEventTypeBinding() {
        return eventTypeBinding;
    }

    /**
     * Get an {@link EventMethodMapper} for the interface.
     * @return
     */
    public EventMethodMapper getEventMethodMapper() {
        return eventMethodMapper;
    }

    /**
     * Get a {@link MultiTypeEventDispatcher} for the interface.
     * @return
     */
    public MultiTypeEventDispatcher<T> getEventDispatcher() {
        return eventDispatcher;
    }
}
