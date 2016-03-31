package com.opencredo.concursus.mapping.events.methods.reflection;

import com.opencredo.concursus.data.tuples.TupleSchema;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.EventType;
import com.opencredo.concursus.domain.events.binding.EventTypeBinding;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.events.methods.reflection.dispatching.EventDispatcher;
import com.opencredo.concursus.mapping.events.methods.reflection.dispatching.EventDispatchers;
import com.opencredo.concursus.mapping.events.methods.reflection.dispatching.InitialEventDispatcher;
import com.opencredo.concursus.mapping.events.methods.reflection.interpreting.EventMethodMapping;
import com.opencredo.concursus.mapping.events.methods.reflection.interpreting.EventMethodType;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

public final class StateClassInfo<T> {

    private static final ConcurrentMap<Class<?>, StateClassInfo<?>> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> StateClassInfo<T> forStateClass(Class<? extends T> stateClass) {
        return (StateClassInfo<T>) cache.computeIfAbsent(stateClass, StateClassInfo::forStateClassUncached);
    }

    private static <T> StateClassInfo<T> forStateClassUncached(Class<? extends T> stateClass) {
        checkNotNull(stateClass, "stateClass must not be null");
        checkArgument(stateClass.isAnnotationPresent(HandlesEventsFor.class),
                "stateClass %s is not annotated with @HandlesEventsFor", stateClass);

        String aggregateType = stateClass.getAnnotation(HandlesEventsFor.class).value();

        Map<Method, EventMethodMapping> factoryMethodMappings = EventMethodType.FACTORY.getEventMethodInfo(aggregateType, stateClass);
        Map<Method, EventMethodMapping> updateMethodMappings = EventMethodType.UPDATER.getEventMethodInfo(aggregateType, stateClass);

        Collection<? extends EventMethodMapping> typeMappings = getTypeMappings(factoryMethodMappings, updateMethodMappings);
        Map<EventType, TupleSchema> eventTypeMappings = EventMethodMapping.getEventTypeMappings(typeMappings);

        return new StateClassInfo<>(
                eventTypeMappings,
                EventTypeBinding.of(aggregateType, EventTypeMatcher.matchingAgainst(eventTypeMappings)),
                EventMethodMapping.makeCausalOrdering(typeMappings),
                EventDispatchers.dispatchingInitialEventsByType(stateClass, factoryMethodMappings),
                EventDispatchers.dispatchingEventsByType(updateMethodMappings));
    }

    private static Collection<? extends EventMethodMapping> getTypeMappings(Map<Method, EventMethodMapping> factoryMethodMappings, Map<Method, EventMethodMapping> updateMethodMappings) {
        return concat(
                    factoryMethodMappings.values().stream(),
                    updateMethodMappings.values().stream()).collect(toList());
    }

    private final Map<EventType, TupleSchema> eventTypeMappings;
    private final EventTypeBinding eventTypeBinding;
    private final Comparator<Event> causalOrder;
    private final InitialEventDispatcher<T> initialEventDispatcher;
    private final EventDispatcher<T> updateEventDispatcher;

    public StateClassInfo(Map<EventType, TupleSchema> eventTypeMappings, EventTypeBinding eventTypeBinding, Comparator<Event> causalOrder, InitialEventDispatcher<T> initialEventDispatcher, EventDispatcher<T> updateEventDispatcher) {
        this.eventTypeMappings = eventTypeMappings;
        this.eventTypeBinding = eventTypeBinding;
        this.causalOrder = causalOrder;
        this.initialEventDispatcher = initialEventDispatcher;
        this.updateEventDispatcher = updateEventDispatcher;
    }

    public Map<EventType, TupleSchema> getEventTypeMappings() {
        return eventTypeMappings;
    }

    public EventTypeBinding getEventTypeBinding() {
        return eventTypeBinding;
    }

    public Comparator<Event> getCausalOrder() {
        return causalOrder;
    }

    public InitialEventDispatcher<T> getInitialEventDispatcher() {
        return initialEventDispatcher;
    }

    public EventDispatcher<T> getUpdateEventDispatcher() {
        return updateEventDispatcher;
    }
}
