package com.opencredo.concursus.data.tuples;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A name/value pair which can be added to a {@link Map}
 */
@FunctionalInterface
public interface NamedValue extends Consumer<Map<String, Object>> {

    /**
     * Create a new name/value pair
     * @param name
     * @param value
     * @return The constructed name/value pair
     */
    static NamedValue of(String name, Object value) {
        return m -> m.put(name, value);
    }

}
