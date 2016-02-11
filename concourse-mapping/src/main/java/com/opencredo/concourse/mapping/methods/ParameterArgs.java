package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.data.tuples.TupleSlot;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class ParameterArgs {

    public static ParameterArgs forMethod(Method method) {
        Type[] parameterTypes = Stream.of(method.getGenericParameterTypes())
                .skip(2)
                .toArray(Type[]::new);

        String[] parameterNames = Stream.of(method.getParameters())
                .skip(2)
                .map(EventInterfaceReflection::getParameterName)
                .toArray(String[]::new);

        return new ParameterArgs(parameterTypes, parameterNames);
    }

    private final Type[] parameterTypes;
    private final String[] parameterNames;

    private ParameterArgs(Type[] parameterTypes, String[] parameterNames) {
        this.parameterTypes = parameterTypes;
        this.parameterNames = parameterNames;
    }

    private <T> Stream<T> streamOverNamesAndTypes(BiFunction<String, Type, T> combiner) {
        return IntStream.range(0, parameterNames.length)
                .mapToObj(i -> combiner.apply(parameterNames[i], parameterTypes[i]));
    }

    private TupleSlot[] getTupleSlots() {
        return streamOverNamesAndTypes(TupleSlot::of)
                .sorted(Comparator.comparing(TupleSlot::getName))
                .toArray(TupleSlot[]::new);
    }

    public TupleSchema getTupleSchema() {
        return TupleSchema.of(getTupleSlots());
    }

    public TupleKey[] getTupleKeys(TupleSchema schema) {
        return streamOverNamesAndTypes(schema::getKey).toArray(TupleKey[]::new);
    }
}
