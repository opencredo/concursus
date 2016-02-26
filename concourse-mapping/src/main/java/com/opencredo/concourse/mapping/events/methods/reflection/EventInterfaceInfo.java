package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toMap;

public final class EventInterfaceInfo<T> {

    private static final ConcurrentMap<Class<?>, EventInterfaceInfo<?>> cache = new ConcurrentHashMap<>();

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
        Map<Method, EventMethodMapping> eventMappers = getEventMappers(iface, aggregateType);

        EventMethodMapper eventMethodMapper = EventMethodMapper.mappingWith(eventMappers);
        MultiEventDispatcher<T> eventDispatcher = getMethodMappingEventDispatcher(eventMappers);

        Comparator<Event> causalOrderComparator = Comparator.comparing(Event::getEventTimestamp); // TODO: collect causal ordering annotations
        EventTypeMatcher eventTypeMatcher = EventTypeMatcher.matchingAgainst(getTupleSchemas(eventMappers));

        return new EventInterfaceInfo<>(
                EventTypeBinding.of(aggregateType, eventTypeMatcher),
                eventMethodMapper,
                eventDispatcher,
                causalOrderComparator);
    }

    private static <T> MultiEventDispatcher<T> getMethodMappingEventDispatcher(Map<Method, EventMethodMapping> methodMappings) {
        return EventTypeMappingEventDispatcher.mapping(methodMappings.entrySet().stream().collect(toMap(
                    e -> e.getValue().getEventType(),
                    e -> MethodInvokingEventDispatcher.dispatching(e.getKey(), e.getValue()))));
    }

    private static Map<Method, EventMethodMapping> getEventMappers(Class<?> iface, String aggregateType) {
        return Stream.of(iface.getMethods())
                .filter(EventInterfaceInfo::isEventEmittingMethod)
                .distinct()
                .collect(toMap(
                        Function.identity(),
                        method -> EventMethodMapping.forMethod(method, aggregateType)
                ));
    }

    private static Map<EventType, TupleSchema> getTupleSchemas(Map<Method, EventMethodMapping> methodMappings) {
        return methodMappings.values().stream()
                .collect(toMap(EventMethodMapping::getEventType, EventMethodMapping::getTupleSchema));
    }

    private static boolean isEventEmittingMethod(Method method) {
        return method.getReturnType().equals(void.class)
                && method.getParameterCount() >= 2
                && method.getParameterTypes()[0].equals(StreamTimestamp.class)
                && method.getParameterTypes()[1].equals(UUID.class);
    }

    private final EventTypeBinding eventTypeBinding;
    private final EventMethodMapper eventMethodMapper;
    private final MultiEventDispatcher<T> eventDispatcher;
    private final Comparator<Event> causalOrderComparator;

    private EventInterfaceInfo(EventTypeBinding eventTypeBinding, EventMethodMapper eventMethodMapper, MultiEventDispatcher<T> eventDispatcher, Comparator<Event> causalOrderComparator) {
        this.eventTypeBinding = eventTypeBinding;
        this.eventMethodMapper = eventMethodMapper;
        this.eventDispatcher = eventDispatcher;
        this.causalOrderComparator = causalOrderComparator;
    }

    public Comparator<Event> getCausalOrderComparator() {
        return causalOrderComparator;
    }

    public EventTypeBinding getEventTypeBinding() {
        return eventTypeBinding;
    }

    public EventMethodMapper getEventMethodMapper() {
        return eventMethodMapper;
    }

    public MultiEventDispatcher<T> getEventDispatcher() {
        return eventDispatcher;
    }
}
