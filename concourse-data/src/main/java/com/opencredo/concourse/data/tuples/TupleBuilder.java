package com.opencredo.concourse.data.tuples;

import java.util.Map;
import java.util.function.Consumer;

@FunctionalInterface
public interface TupleBuilder extends Consumer<Map<String, Object>> {

    static TupleBuilder of(String name, Object value) {
        return m -> m.put(name, value);
    }

}
