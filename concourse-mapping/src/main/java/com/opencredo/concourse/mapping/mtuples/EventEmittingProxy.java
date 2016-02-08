package com.opencredo.concourse.mapping.mtuples;

import com.google.common.base.Preconditions;
import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.data.tuples.TupleKeyValue;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.data.tuples.TupleSlot;
import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.StreamTimestamp;
import com.opencredo.concourse.domain.VersionedName;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.annotations.Name;
import com.opencredo.concourse.mapping.annotations.Version;

import java.lang.reflect.*;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class EventEmittingProxy implements InvocationHandler {

    public static <T> T proxying(Consumer<Event> eventConsumer, Class<T> klass) {
        return klass.cast(Proxy.newProxyInstance(klass.getClassLoader(),
                new Class<?>[] { klass },
                new EventEmittingProxy(eventConsumer, getEventMappers(klass))
        ));
    }

    public static final class MethodArgsEventMapper implements Function<Object[], Event> {

        public MethodArgsEventMapper(String aggregateType, VersionedName eventName, TupleSchema tupleSchema, TupleKey[] tupleKeys) {
            this.aggregateType = aggregateType;
            this.eventName = eventName;
            this.tupleSchema = tupleSchema;
            this.tupleKeys = tupleKeys;
        }

        public static MethodArgsEventMapper forMethod(Method method, Class<?> klass) {
            String aggregateType = klass.getAnnotation(HandlesEventsFor.class).value();
            String name = method.isAnnotationPresent(Name.class)
                    ? method.getAnnotation(Name.class).value()
                    : method.getName();
            String version = method.isAnnotationPresent(Version.class)
                    ? method.getAnnotation(Version.class).value()
                    : "0" ;
            Type[] parameterTypes = Stream.of(method.getGenericParameterTypes()).skip(2).toArray(Type[]::new);
            String[] parameterNames = Stream.of(method.getParameters())
                    .skip(2)
                    .map(MethodArgsEventMapper::getParameterName)
                    .toArray(String[]::new);

            TupleSlot[] tupleSlots = IntStream.range(0, parameterTypes.length).mapToObj(i ->
            TupleSlot.of(parameterNames[i], parameterTypes[i]))
                    .sorted(Comparator.comparing(TupleSlot::getName))
                    .toArray(TupleSlot[]::new);

            TupleSchema schema = TupleSchema.of(tupleSlots);
            TupleKey[] keys = IntStream.range(0, parameterTypes.length).mapToObj(i ->
                    schema.getKey(parameterNames[i], parameterTypes[i])).toArray(TupleKey[]::new);

            return new MethodArgsEventMapper(
                    aggregateType,
                    VersionedName.of(name, version),
                    schema,
                    keys);
        }

        private static String getParameterName(Parameter parameter) {
            return parameter.isAnnotationPresent(Name.class)
                    ? parameter.getAnnotation(Name.class).value()
                    : parameter.getName();
        }

        private final String aggregateType;
        private final VersionedName eventName;
        private final TupleSchema tupleSchema;
        private final TupleKey[] tupleKeys;

        @Override
        public Event apply(Object[] args) {
            return Event.of(
                    AggregateId.of(aggregateType, (UUID) args[1]),
                    (StreamTimestamp) args[0],
                    eventName,
                    tupleSchema.make(IntStream.range(0, tupleKeys.length)
                            .mapToObj(getValueFrom(args))
                            .toArray(TupleKeyValue[]::new))
            );
        }

        @SuppressWarnings("unchecked")
        private IntFunction<TupleKeyValue> getValueFrom(Object[] args) {
            return i -> tupleKeys[i].of(args[i + 2]);
        }
    }

    private static final ConcurrentMap<Class<?>, Map<Method, MethodArgsEventMapper>> cache = new ConcurrentHashMap<>();

    private static Map<Method, MethodArgsEventMapper> getEventMappers(Class<?> klass) {
        return cache.computeIfAbsent(klass, EventEmittingProxy::getEventMappersUncached);
    }

    private static Map<Method, MethodArgsEventMapper> getEventMappersUncached(Class<?> klass) {
        return Stream.of(klass.getMethods())
                .filter(EventEmittingProxy::isEventEmittingMethod)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        m -> MethodArgsEventMapper.forMethod(m, klass)
                ));
    }

    private static boolean isEventEmittingMethod(Method method) {
        return method.getReturnType().equals(void.class)
                && method.getParameterCount() >= 2
                && method.getParameterTypes()[0].equals(StreamTimestamp.class)
                && method.getParameterTypes()[1].equals(UUID.class);
    }

    private final Consumer<Event> eventConsumer;
    private final Map<Method, MethodArgsEventMapper> eventMappers;

    private EventEmittingProxy(Consumer<Event> eventConsumer, Map<Method, MethodArgsEventMapper> eventMappers) {
        this.eventConsumer = eventConsumer;
        this.eventMappers = eventMappers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().isAssignableFrom(getClass())) {
            return method.invoke(this, args);
        }
        MethodArgsEventMapper mapper = eventMappers.get(method);
        Preconditions.checkState(mapper != null, "No mapper found for method %s", method);

        eventConsumer.accept(mapper.apply(args));
        return null;
    }
}
