package com.opencredo.concourse.mapping.pojos;

import com.google.common.base.Preconditions;
import com.opencredo.concourse.domain.VersionedName;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.mapping.annotations.Name;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PojoEventConverter<B> implements Function<Event, PojoEvent<? extends B>> {

    @SafeVarargs
    public static <B> PojoEventConverter<B> mapping(Class<? extends B>...classes) {
        return mapping(Stream.of(classes));
    }

    public static <B> PojoEventConverter<B> mapping(Collection<Class<? extends B>> classes) {
        return mapping(classes.stream());
    }

    private static <B> PojoEventConverter<B> mapping(Stream<Class<? extends B>> classes) {
        return new PojoEventConverter<>(classes.collect(Collectors.toMap(
                PojoEventConverter::getVersionedName,
                Function.identity()
        )));
    }

    private static VersionedName getVersionedName(Class<?> klass) {
        return klass.isAnnotationPresent(Name.class)
                ? VersionedName.of(
                    klass.getAnnotation(Name.class).value(),
                    klass.getAnnotation(Name.class).version())
                : VersionedName.of(
                    klass.getSimpleName().substring(0, 1).toLowerCase() + klass.getSimpleName().substring(1),
                    "0");
    }

    private final Map<VersionedName, Class<? extends B>> classLookup;

    private PojoEventConverter(Map<VersionedName, Class<? extends B>> classLookup) {
        this.classLookup = classLookup;
    }

    @Override
    public PojoEvent<? extends B> apply(Event event) {
        VersionedName eventName = event.getEventName();
        Preconditions.checkArgument(classLookup.containsKey(eventName),
                "No class registered for event %s", eventName);

        return PojoEvent.of(event, classLookup.get(eventName));
    }

}
