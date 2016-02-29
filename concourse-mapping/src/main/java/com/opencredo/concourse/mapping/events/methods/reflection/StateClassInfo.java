package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.domain.events.binding.EventTypeBinding;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.EventDispatcher;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.EventDispatchers;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.InitialEventDispatcher;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.EventInterpreters;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.TypeMapping;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.TypeMappingEventInterpreter;

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

        Map<Method, TypeMappingEventInterpreter> factoryMethodMappings = getFactoryMethodMappings(stateClass, aggregateType);
        Map<Method, TypeMappingEventInterpreter> updateMethodMappings = getUpdateMethodMappings(stateClass, aggregateType);

        Collection<? extends TypeMapping> typeMappings = getTypeMappings(factoryMethodMappings, updateMethodMappings);

        return new StateClassInfo<>(
                makeEventTypeBinding(aggregateType, typeMappings),
                TypeMapping.makeCausalOrdering(typeMappings),
                EventDispatchers.dispatchingInitialEventsByType(stateClass, factoryMethodMappings),
                EventDispatchers.dispatchingEventsByType(updateMethodMappings));
    }

    private static Collection<? extends TypeMapping> getTypeMappings(Map<Method, TypeMappingEventInterpreter> factoryMethodMappings, Map<Method, TypeMappingEventInterpreter> updateMethodMappings) {
        return concat(
                    factoryMethodMappings.values().stream(),
                    updateMethodMappings.values().stream()).collect(toList());
    }

    private static EventTypeBinding makeEventTypeBinding(String aggregateType, Collection<? extends TypeMapping> typeMappings) {
        return EventTypeBinding.of(aggregateType, TypeMapping.makeEventTypeMatcher(typeMappings));
    }

    private static <T> Map<Method, TypeMappingEventInterpreter> getFactoryMethodMappings(Class<? extends T> stateClass, String aggregateType) {
        return MethodSelectors.interpretMethods(
                stateClass,
                MethodSelectors.isFactoryMethod,
                method -> EventInterpreters.forFactoryMethod(stateClass, aggregateType, method));
    }

    private static <T> Map<Method, TypeMappingEventInterpreter> getUpdateMethodMappings(Class<? extends T> stateClass, String aggregateType) {
        return MethodSelectors.interpretMethods(
                stateClass,
                MethodSelectors.isUpdateMethod,
                method -> EventInterpreters.forUpdateMethod(aggregateType, method));
    }

    private final EventTypeBinding eventTypeBinding;
    private final Comparator<Event> causalOrder;
    private final InitialEventDispatcher<T> initialEventDispatcher;
    private final EventDispatcher<T> updateEventDispatcher;

    public StateClassInfo(EventTypeBinding eventTypeBinding, Comparator<Event> causalOrder, InitialEventDispatcher<T> initialEventDispatcher, EventDispatcher<T> updateEventDispatcher) {
        this.eventTypeBinding = eventTypeBinding;
        this.causalOrder = causalOrder;
        this.initialEventDispatcher = initialEventDispatcher;
        this.updateEventDispatcher = updateEventDispatcher;
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
