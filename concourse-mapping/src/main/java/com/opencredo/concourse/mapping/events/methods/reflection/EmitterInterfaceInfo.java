package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.domain.events.binding.EventTypeBinding;
import com.opencredo.concourse.domain.events.matching.EventTypeMatcher;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.EventDispatchers;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.MultiTypeEventDispatcher;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.EventMethodMapping;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.EventMethodType;

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
public final class EmitterInterfaceInfo<T> {

    private static final ConcurrentMap<Class<?>, EmitterInterfaceInfo<?>> cache = new ConcurrentHashMap<>();

    /**
     * Get event interface information from the supplied interface. This method is cached.
     * @param iface The interface to get information for.
     * @param <T> The type of the interface.
     * @return The EventInterfaceInfo for the supplied interface.
     */
    @SuppressWarnings("unchecked")
    public static <T> EmitterInterfaceInfo<T> forInterface(Class<? extends T> iface) {
        return (EmitterInterfaceInfo<T>) cache.computeIfAbsent(iface, EmitterInterfaceInfo::forInterfaceUncached);
    }

    private static <T> EmitterInterfaceInfo<T> forInterfaceUncached(Class<? extends T> iface) {
        checkNotNull(iface, "iface must not be null");
        checkArgument(iface.isInterface(), "iface must be interface");
        checkArgument(iface.isAnnotationPresent(HandlesEventsFor.class),
                "Interface %s is not annotated with @HandlesEventsFor", iface);

        String aggregateType = iface.getAnnotation(HandlesEventsFor.class).value();
        Map<Method, EventMethodMapping> eventMappers = EventMethodType.EMITTER.getEventMethodInfo(aggregateType, iface);

        Map<EventType, TupleSchema> eventTypeMatcherMap = EventMethodMapping.getEventTypeMappings(eventMappers.values());

        return new EmitterInterfaceInfo<>(
                eventTypeMatcherMap,
                EventTypeBinding.of(aggregateType, EventTypeMatcher.matchingAgainst(eventTypeMatcherMap)),
                EventMethodMapper.mappingWith(eventMappers),
                EventDispatchers.dispatchingEventsByType(eventMappers),
                EventMethodMapping.makeCausalOrdering(eventMappers.values()));
    }

    private final Map<EventType, TupleSchema> eventTypeMatcherMap;
    private final EventTypeBinding eventTypeBinding;
    private final EventMethodMapper eventMethodMapper;
    private final MultiTypeEventDispatcher<T> eventDispatcher;
    private final Comparator<Event> causalOrderComparator;

    private EmitterInterfaceInfo(Map<EventType, TupleSchema> eventTypeMatcherMap, EventTypeBinding eventTypeBinding, EventMethodMapper eventMethodMapper, MultiTypeEventDispatcher<T> eventDispatcher, Comparator<Event> causalOrderComparator) {
        this.eventTypeMatcherMap = eventTypeMatcherMap;
        this.eventTypeBinding = eventTypeBinding;
        this.eventMethodMapper = eventMethodMapper;
        this.eventDispatcher = eventDispatcher;
        this.causalOrderComparator = causalOrderComparator;
    }

    /**
     * Get a {@link Map} of {@link EventType}s to {@link TupleSchema}s that can be used to build an {@link EventTypeMatcher}
     * @return
     */
    public Map<EventType, TupleSchema> getEventTypeMatcherMap() {
        return eventTypeMatcherMap;
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

    /**
     * Get an {@link EventTypeMatcher} for the interface.
     * @return
     */
    public EventTypeMatcher getEventTypeMatcher() {
        return EventTypeMatcher.matchingAgainst(eventTypeMatcherMap);
    }
}
