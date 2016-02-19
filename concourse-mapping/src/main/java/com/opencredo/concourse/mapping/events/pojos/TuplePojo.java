package com.opencredo.concourse.mapping.events.pojos;

import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.data.tuples.TupleSchema;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TuplePojo implements InvocationHandler {

    public static <T> T wrapping(Tuple tuple, Class<? extends T> pojoClass) {
        return pojoClass.cast(Proxy.newProxyInstance(pojoClass.getClassLoader(),
                new Class<?>[] { pojoClass },
                new TuplePojo(tuple, getTupleMap(pojoClass, tuple.getSchema()))));
    }

    private static final ConcurrentMap<Class, ConcurrentMap<TupleSchema, Map<Method, TupleKey>>> cache =
            new ConcurrentHashMap<>();

    private static Map<Method, TupleKey> getTupleMap(Class pojoClass, TupleSchema mappedSchema) {
        return cache.computeIfAbsent(pojoClass, cls -> new ConcurrentHashMap<>())
                .computeIfAbsent(mappedSchema, schema -> getTupleMapUncached(pojoClass, mappedSchema));
    }

    private static Map<Method, TupleKey> getTupleMapUncached(Class pojoClass, TupleSchema mappedSchema) {
        return Stream.of(safeGetBeanInfo(pojoClass).getPropertyDescriptors())
                .collect(Collectors.toMap(
                        PropertyDescriptor::getReadMethod,
                        pd -> mappedSchema.getKey(pd.getName(), pd.getReadMethod().getGenericReturnType())
                ));
    }

    private static BeanInfo safeGetBeanInfo(Class pojoClass) {
        try {
            return Introspector.getBeanInfo(pojoClass);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    private final Tuple tuple;
    private final Map<Method, TupleKey> keyMap;

    public TuplePojo(Tuple tuple, Map<Method, TupleKey> keyMap) {
        this.tuple = tuple;
        this.keyMap = keyMap;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().isAssignableFrom(getClass())) {
            return method.invoke(this, args);
        }
        return tuple.get(keyMap.get(method));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || ! Proxy.isProxyClass(o.getClass())) {
            return false;
        }
        Object invocationHandler = Proxy.getInvocationHandler(o);
        return invocationHandler instanceof TuplePojo
                && ((TuplePojo) invocationHandler).tuple.equals(tuple);
    }

    @Override
    public int hashCode() {
        return tuple.hashCode();
    }

    @Override
    public String toString() {
        return tuple.toString();
    }
}
