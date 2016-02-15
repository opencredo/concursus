package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.data.tuples.TupleSchemaRegistry;

public final class EventProxyClassRegistry {

    public static EventProxyClassRegistry registeringSchemasWith(TupleSchemaRegistry tupleSchemaRegistry) {
        return new EventProxyClassRegistry(tupleSchemaRegistry);
    }

    private final TupleSchemaRegistry tupleSchemaRegistry;

    private EventProxyClassRegistry(TupleSchemaRegistry tupleSchemaRegistry) {
        this.tupleSchemaRegistry = tupleSchemaRegistry;
    }

    public EventProxyClassRegistry register(Class<?> proxyClass) {
        EventInterfaceReflection.getEventMappers(proxyClass).values().forEach(m -> m.registerSchema(tupleSchemaRegistry));
        return this;
    }
}
