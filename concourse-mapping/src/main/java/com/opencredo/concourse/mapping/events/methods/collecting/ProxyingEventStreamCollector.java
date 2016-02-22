package com.opencredo.concourse.mapping.events.methods.collecting;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventReplayer;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.mapping.events.methods.dispatching.EventMethodDispatcher;
import com.opencredo.concourse.mapping.events.methods.reflection.EventInterfaceReflection;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ProxyingEventStreamCollector<T, I, A> {

    public static <T, I, A> ProxyingEventStreamCollector<T, I, A> proxying(Class<? extends T> stateClass, Class<? extends I> initialiserHandlerClass, Class<? extends A> accumulatorHandlerClass) {
        return new ProxyingEventStreamCollector<>(
                EventInterfaceReflection.getAggregateType(initialiserHandlerClass),
                EventInterfaceReflection.getEventTypeMatcher(initialiserHandlerClass, accumulatorHandlerClass),
                initialiserHandlerClass,
                accumulatorHandlerClass
        );
    }

    private final String aggregateType;
    private final EventTypeMatcher eventTypeMatcher;
    private final Class<? extends I> initialiserHandlerClass;
    private final Class<? extends A> accumulatorHandlerClass;

    private ProxyingEventStreamCollector(String aggregateType, EventTypeMatcher eventTypeMatcher, Class<? extends I> initialiserHandlerClass, Class<? extends A> accumulatorHandlerClass) {
        this.aggregateType = aggregateType;
        this.eventTypeMatcher = eventTypeMatcher;
        this.initialiserHandlerClass = initialiserHandlerClass;
        this.accumulatorHandlerClass = accumulatorHandlerClass;
    }

    public Optional<T> collect(CachedEventSource eventSource, UUID aggregateId, Function<Consumer<T>, I> initialiserBuilder, Function<T, A> accumulatorBuilder) {
        return collect(eventSource.replaying(addTypeTo(aggregateId)).inAscendingOrder(), initialiserBuilder, accumulatorBuilder);
    }

    public Optional<T> collect(EventSource eventSource, UUID aggregateId, Function<Consumer<T>, I> initialiserBuilder, Function<T, A> accumulatorBuilder) {
        return collect(eventSource.replaying(eventTypeMatcher, addTypeTo(aggregateId)).inAscendingOrder(),
                initialiserBuilder,
                accumulatorBuilder);
    }

    public Optional<T> collect(EventSource eventSource, UUID aggregateId, TimeRange timeRange, Function<Consumer<T>, I> initialiserBuilder, Function<T, A> accumulatorBuilder) {
        return collect(eventSource.replaying(eventTypeMatcher, addTypeTo(aggregateId), timeRange).inAscendingOrder(),
                initialiserBuilder,
                accumulatorBuilder);
    }

    public Optional<T> collect(Function<EventTypeMatcher, EventReplayer> eventReplayerBuilder, Function<Consumer<T>, I> initialiserBuilder, Function<T, A> accumulatorBuilder) {
        return collect(eventReplayerBuilder.apply(eventTypeMatcher), initialiserBuilder, accumulatorBuilder);
    }

    public Optional<T> collect(EventReplayer eventReplayer, Function<Consumer<T>, I> initialiserBuilder, Function<T, A> accumulatorBuilder) {
        Iterator<Event> eventIterator = eventReplayer.toList().iterator();

        return getInitial(eventIterator, initialiserBuilder).map(item -> {
            EventMethodDispatcher accumulatorDispatcher = EventMethodDispatcher.toHandler(accumulatorHandlerClass, accumulatorBuilder.apply(item));
            eventIterator.forEachRemaining(accumulatorDispatcher);
            return item;
        });
    }

    private Optional<T> getInitial(Iterator<Event> events, Function<Consumer<T>, I> initialiserBuilder) {
        if (!events.hasNext()) {
            return Optional.empty();
        }

        AtomicReference<T> ref = new AtomicReference<>();

        final Consumer<Event> firstEventConsumer = EventMethodDispatcher
                .toCollector(initialiserHandlerClass, initialiserBuilder)
                .apply(ref::set);

        firstEventConsumer.accept(events.next());

        return Optional.ofNullable(ref.get());
    }

    public AggregateId addTypeTo(UUID aggregateId) {
        return AggregateId.of(aggregateType, aggregateId);
    }

    public EventTypeMatcher getEventTypeMatcher() {
        return eventTypeMatcher;
    }

    public String getAggregateType() {
        return aggregateType;
    }
}
