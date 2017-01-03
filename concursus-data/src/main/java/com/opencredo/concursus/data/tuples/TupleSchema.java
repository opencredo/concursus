package com.opencredo.concursus.data.tuples;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * An ordered collection of TupleSlots, defining what may be stored in a conforming Tuple.
 */
public final class TupleSchema {

    /**
     * Create a TupleSchema having the supplied TupleSlots.
     * @param slots The TupleSlots in the schema.
     * @return The created TupleSchema.
     */
    public static TupleSchema of(String name, TupleSlot...slots) {
        if (name == null) throw new IllegalArgumentException("name must not be null");
        if (slots == null) throw new IllegalArgumentException("slots must not be null");

        Map<String, Integer> slotLookup = IntStream.range(0, slots.length)
                .collect(HashMap::new, (m, i) -> m.put(slots[i].getName(), i), Map::putAll);

        if (slots.length != slotLookup.size()) throw new IllegalArgumentException("Slot names are not unique");
        return new TupleSchema(name, slots, slotLookup);
    }

    private final String name;
    private final TupleSlot[] slots;
    private final Map<String, Integer> slotLookup;

    private TupleSchema(String name, TupleSlot[] slots, Map<String, Integer> slotLookup) {
        this.name = name;
        this.slots = slots;
        this.slotLookup = slotLookup;
    }

    public String getName() {
        return name;
    }

    /**
     * Make a tuple of the supplied values, first validating that they conform to this schema.
     * @param values The values to put in the Tuple.
     * @return The created Tuple.
     */
    public Tuple makeWith(Object...values) {
        return make(values);
    }

    /**
     * Make a tuple of the supplied values, first validating that they conform to this schema.
     * @param values The values to put in the Tuple.
     * @return The created Tuple.
     */
    public Tuple make(Object[] values) {
        if (values == null) throw new IllegalArgumentException("value must not be null");
        if (values.length != slots.length)
                throw new IllegalArgumentException(
                        String.format("Expected %s values, but received %s", slots.length, values.length));
        if (!typesMatch(values)) {
            throw new IllegalArgumentException(describeTypeMismatches(values));
        }

        return new Tuple(this, values);
    }

    /**
     * Build a tuple using the supplied key/value pairs, first validating that the keys belong to this schema and are complete.
     * @param keyValues The TupleKeyValues to use to create the Tuple.
     * @return The created Tuple.
     */
    public Tuple make(TupleKeyValue...keyValues) {
        if (keyValues == null) throw new IllegalArgumentException("keyValues must not be null");
        if (!Stream.of(keyValues).allMatch(kv -> kv.belongsToSchema(this))) {
            throw new IllegalArgumentException(String.format(
                    "Keys %s do not all belong to schema %s",
                    getKeyNames(keyValues), this));
        }
        if (Stream.of(keyValues).map(TupleKeyValue::getTupleKey).distinct().count() != slots.length) {
            throw new IllegalArgumentException(String.format(
                    "Not all slots in %s filled by provided keys %s",
                    this, getKeyNames(keyValues)));
        }

        Object[] values = new Object[slots.length];
        Stream.of(keyValues).forEach(kv -> kv.build(values));
        return new Tuple(this, values);
    }

    private String getKeyNames(TupleKeyValue[] keyValues) {
        return Stream.of(keyValues)
                .map(TupleKeyValue::getTupleKey)
                .map(Object::toString)
                .collect(joining(",", "[", "]"));
    }

    /**
     * Build a tuple using a map of tuple values constructed by the supplied builders.
     * @param builders The builders to use to build up a map of tuple values.
     * @return The created tuple.
     */
    public Tuple make(NamedValue...builders) {
        Map<String, Object> values = new HashMap<>();
        Stream.of(builders).forEach(builder -> builder.accept(values));
        return make(values);
    }

    /**
     * Create a tuple from a map of name/value pairs.
     * @param values The values to put in the tuple.
     * @return The created tuple.
     */
    public Tuple make(Map<String, Object> values) {
        if (values == null) throw new IllegalArgumentException("values must not be null");
        checkMatchingKeys(values);

        Object[] valueArray = new Object[slots.length];
        getIndices().forEach(i -> valueArray[i] = values.get(slots[i].getName()));

        return make(valueArray);
    }

    /**
     * Create a tuple using the supplied deserialiser, out of a map of serialised values.
     * @param deserialiser The deserialiser to use to deserialise values from the map.
     * @param values A map of serialised tuple values.
     * @param <V> The type to which tuple value have been serialised, e.g. String.
     * @return The created tuple.
     */
    public <V> Tuple deserialise(BiFunction<V, Type, Object> deserialiser, Map<String, V> values) {
        if (deserialiser == null) throw new IllegalArgumentException("deserialiser must not be null");
        if (values == null) throw new IllegalArgumentException("values must not be null");
        checkMatchingKeys(values);

        Object[] valueArray = new Object[slots.length];
        getIndices().forEach(i -> valueArray[i] = slots[i].deserialise(deserialiser, values));

        return make(valueArray);
    }

    private void checkMatchingKeys(Map<String, ?> values) {
        if (!values.keySet().equals(slotLookup.keySet()))
                throw new IllegalArgumentException(String.format(
                        "Expected keys %s, but were %s", slotLookup.keySet(), values.keySet()));
    }

    Object get(String name, Object[] values) {
        Integer valueIndex = slotLookup.get(name);
        if (valueIndex == null) throw new IllegalArgumentException(
                String.format("Schema %s does not have a slot named '%s'", this, name));

        return values[valueIndex];
    }

    private boolean typesMatch(Object[] values) {
        return getIndices().allMatch(i -> slots[i].accepts(values[i]));
    }

    private String describeTypeMismatches(Object[] values) {
        return getIndices()
                .filter(i -> !slots[i].accepts(values[i]))
                .mapToObj(i -> String.format("Slot (%s) does not accept value <%s>", slots[i], values[i]))
                .collect(joining(", "));
    }

    <V> Map<String, V> serialise(Function<Object, V> serialiser, Object[] values) {
        return getIndices()
                .collect(LinkedHashMap::new, (m, i) -> m.put(slots[i].getName(), serialiser.apply(values[i])), Map::putAll);
    }

    Map<String, Object> toMap(Object[] values) {
        return getIndices()
                .collect(LinkedHashMap::new, (m, i) -> m.put(slots[i].getName(), values[i]), Map::putAll);
    }

    private IntStream getIndices() {
        return IntStream.range(0, slots.length);
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || (o instanceof TupleSchema
                    && name.equals(((TupleSchema) o).name)
                    && Arrays.deepEquals(((TupleSchema) o).slots, slots));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.deepHashCode(slots));
    }

    @Override
    public String toString() {
        return name + Stream.of(slots).map(Object::toString).collect(joining(",", "{", "}"));
    }

    String format(Object[] values) {
        return name + getIndices()
                .mapToObj(i -> slots[i].getName() + "=" + values[i])
                .collect(joining(", ", "{", "}"));
    }

    /**
     * Get a key which can be used to retrieve an Optional value from a tuple in a type-safe way, without having to do an index lookup.
     * @param name The name of the slot to get a key for.
     * @param valueType The class of the Optional value to retrieve with the key.
     * @param <T> The type of the Optinoal value to retrieve with the key.
     * @return The created key.
     */
    public <T> TupleKey<Optional<T>> getOptionalKey(String name, Class<T> valueType) {
        return getKey(name, Types.optionalOf(valueType));
    }

    /**
     * Get a key which can be used to retrieve a list of value from a tuple in a type-safe way, without having to do an index lookup.
     * @param name The name of the slot to get a key for.
     * @param elementType The element class of the values to retrieve with the key.
     * @param <T> The element type of the values to retrieve with the key.
     * @return The created key.
     */
    public <T> TupleKey<List<T>> getListKey(String name, Class<T> elementType) {
        return getKey(name, Types.listOf(elementType));
    }

    /**
     * Get a key which can be used to retrieve a map of values from a tuple in a type-safe way, without having to do an index lookup.
     * @param name The name of the slot to get a key for.
     * @param keyType The key class of the map to retrieve with the key.
     * @param valueType The value class of the map to retrieve with the key.
     * @param <K> The key type of the value to retrieve with the key.
     * @param <V> The value type of the value to retrieve with the key.
     * @return The created key.
     */
    public <K, V> TupleKey<Map<K, V>> getMapKey(String name, Class<K> keyType, Class<V> valueType) {
        return getKey(name, Types.mapOf(keyType, valueType));
    }

    /**
     * Get a key which can be used to retrieve a value from a tuple in a type-safe way, without having to do an index lookup.
     * @param name The name of the slot to get a key for.
     * @param type The type of the value to retrieve with the key.
     * @param <T> The type of the value to retrieve with the key.
     * @return The created key.
     */
    public <T> TupleKey<T> getKey(String name, Type type) {
        if (name == null) throw new IllegalArgumentException("name must not be null");
        if (type == null) throw new IllegalArgumentException("type must not be null");

        Integer valueIndex = slotLookup.get(name);
        if (valueIndex == null) throw new IllegalArgumentException(name);

        TupleSlot slot = slots[valueIndex];
        if (!slot.acceptsType(type))
                throw new IllegalArgumentException("Slot " + name + " does not accept type " + type);

        return new TupleKey<>(this, name, valueIndex);
    }
}
