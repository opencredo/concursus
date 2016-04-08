package com.opencredo.concursus.mapping.events.methods.proxying;

import com.opencredo.concursus.domain.events.channels.EventOutChannel;
import com.opencredo.concursus.domain.events.channels.RoutingEventOutChannel;
import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.functional.Consumers;
import com.opencredo.concursus.domain.events.state.StateBuilder;
import com.opencredo.concursus.mapping.events.methods.state.DispatchingStateBuilder;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * An {@link EventBus} that generates proxies for event-emitter interfaces.
 */
public interface ProxyingEventBus extends EventBus {

    /**
     * Create a {@link ProxyingEventBus} that dispatches events via the supplied {@link EventBus}
     * @param eventBus The {@link EventBus} to dispatch events to.
     * @return The constructed {@link ProxyingEventBus}.
     */
    static ProxyingEventBus proxying(EventBus eventBus) {
        return eventBus::startBatch;
    }

    /**
     * Create a proxy instance of the supplied interface, and pass it to the supplied {@link Consumer} to emit events.
     * @param klass The interface to proxy.
     * @param dispatcherConsumer The {@link Consumer} that will use the proxy object to emit events.
     * @param <T> The type of the proxy object.
     */
    default <T> void dispatch(Class<T> klass, Consumer<T> dispatcherConsumer) {
        dispatch(Consumers.transform(
                dispatcherConsumer,
                eventConsumer -> EventEmittingProxy.proxying(eventConsumer, klass)));
    }


    /**
     * Create proxy instances of the supplied interfaces, and pass them to the supplied {@link BiConsumer} to emit events.
     * @param leftKlass The "left" interface to proxy.
     * @param rightKlass The "right" interface to proxy.
     * @param dispatchersConsumer The {@link BiConsumer} that will use the proxy objects to emit events.
     * @param <L> The type of the "left" proxy object.
     * @param <R> The type of the "right" proxy object.
     */
    default <L, R> void dispatch(Class<L> leftKlass, Class<R> rightKlass, BiConsumer<L, R> dispatchersConsumer) {
        dispatch(eventConsumer -> dispatchersConsumer.accept(
                EventEmittingProxy.proxying(eventConsumer, leftKlass),
                EventEmittingProxy.proxying(eventConsumer, rightKlass)
        ));
    }

    /**
     * Get a proxy instance of the supplied interface that will create and complete a distinct batch containing a single
     * event for each event-emitting method invocation.
     * @param klass The interface to proxy.
     * @param <T> The type of the proxy object.
     * @return The constructed proxy object.
     */
    default <T> T getDispatcherFor(Class<T> klass) {
        return EventEmittingProxy.proxying(this, klass);
    }


    /**
     * Create a copy of this event bus that additionally sends events to the supplied state instance on batch completion.
     * @param stateInstance The state instance to send events to.
     * @param busConsumer A {@link Consumer} that will use the subscribed bus.
     * @param <S> The type of the state instance.
     */
    default <S> void updating(S stateInstance, Consumer<ProxyingEventBus> busConsumer) {
        updating(stateInstance.getClass(), stateInstance, busConsumer);
    }

    /**
     * Create a copy of this event bus that additionally sends events to the supplied state instance on batch completion.
     * @param stateClass The class of the state instance.
     * @param stateInstance The state instance to send events to.
     * @param busConsumer A {@link Consumer} that will use the subscribed bus.
     * @param <S> The type of the state instance.
     */
    default <S> void updating(Class<? extends S> stateClass, S stateInstance, Consumer<ProxyingEventBus> busConsumer) {
        StateBuilder<S> stateBuilder = DispatchingStateBuilder.dispatchingTo(stateClass, stateInstance);
        notifying(stateBuilder.toEventsOutChannel(), Consumers.transform(busConsumer, ProxyingEventBus::proxying));
    }

    /**
     * Create a copy of this event bus that additionally sends events to the supplied state class on batch completion.
     * @param stateClass The class of the state instance.
     * @param busConsumer A {@link Consumer} that will use the subscribed bus.
     * @param <S> The type of the state instance.
     * @return The constructed state, if present.
     */
    default <S> Optional<S> creating(Class<? extends S> stateClass, Consumer<ProxyingEventBus> busConsumer) {
        StateBuilder<S> stateBuilder = DispatchingStateBuilder.dispatchingTo(stateClass);
        notifying(stateBuilder.toEventsOutChannel(), Consumers.transform(busConsumer, ProxyingEventBus::proxying));
        return stateBuilder.get();
    }

    /**
     * Create a copy of this event bus that additionally sends events to the supplied state objects (mapped by
     * aggregate id) on batch completion.
     * @param stateObjectsById The state objects to send events to on batch completion.
     * @param busConsumer A {@link Consumer} that will use the subscribed bus.
     */
    default void updating(Map<UUID, Object> stateObjectsById, Consumer<ProxyingEventBus> busConsumer) {
        EventOutChannel outChannel = RoutingEventOutChannel.routingWith(stateObjectsById.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> DispatchingStateBuilder.dispatchingTo(e.getValue()))));

        notifying(outChannel.toEventsOutChannel(), Consumers.transform(busConsumer, ProxyingEventBus::proxying));
    }

    /**
     * Create a copy of this event bus that additionally sends events intended for the supplied state object to
     * that object on batch completion. Use this when you will generate additional events that should not be routed
     * to the state object.
     * @param aggregateId The state object's aggregate id. Events not for this aggregate will not be routed to the object.
     * @param stateInstance The state instance to route events to.
     * @param busConsumer A {@link Consumer} that will use the subscribed bus.
     * @param <S> The type of the state instance.
     */
    default <S> void updating(UUID aggregateId, S stateInstance, Consumer<ProxyingEventBus> busConsumer) {
        StateBuilder<S> stateBuilder = DispatchingStateBuilder.dispatchingTo(stateInstance);
        notifying(events -> events.stream()
                .filter(event -> event.getAggregateId().getId().equals(aggregateId))
                .forEach(stateBuilder),
                Consumers.transform(busConsumer, ProxyingEventBus::proxying));
    }

}
