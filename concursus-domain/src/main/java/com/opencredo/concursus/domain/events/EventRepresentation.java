package com.opencredo.concursus.domain.events;

import com.opencredo.concursus.data.tuples.Tuple;
import com.opencredo.concursus.data.tuples.TupleSchema;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.Optional;

public interface EventRepresentation<T> extends HasEventMetadata {

    static <T> EventRepresentation<T> of(EventMetadata metadata, T data) {
        return new Concrete<>(metadata, data);
    }

    class Concrete<T> implements EventRepresentation<T> {
        private final EventMetadata metadata;
        private final T data;

        private Concrete(EventMetadata metadata, T data) {
            this.metadata = metadata;
            this.data = data;
        }

        @Override
        public T getData() {
            return data;
        }

        @Override
        public EventMetadata getMetadata() {
            return metadata;
        }

        @Override
        public boolean equals(Object o) {
            return this == o || (o instanceof EventRepresentation && equals(EventRepresentation.class.cast(o)));
        }

        private boolean equals(EventRepresentation<?> o) {
            return metadata.equals(o.getMetadata())
                    && data.equals(o.getData());
        }

        @Override
        public int hashCode() {
            return Objects.hash(metadata, data);
        }

        @Override
        public String toString() {
            return getProcessingTime().map(processingTime ->
                    String.format("%s\nwith %s\nprocessed at %s",
                            metadata, data, processingTime))
                    .orElseGet(() -> String.format("%s\nwith %s",
                            metadata, data));
        }
    }

    T getData();

    default <O> EventRepresentation<O> map(Function<T, O> dataTransformer) {
        return of(getMetadata(), dataTransformer.apply(getData()));
    }

    default Event toEvent(Function<T, Tuple> tupleFunction) {
        return Event.of(getMetadata(), tupleFunction.apply(getData()));
    }

    default Optional<Event> toEvent(EventTypeMatcher eventTypeMatcher, BiFunction<TupleSchema, T, Tuple> tupleBiFunction) {
        return eventTypeMatcher.match(getType())
                .map(schema ->
                        Event.of(
                                getMetadata(),
                                tupleBiFunction.apply(schema, getData())));
    }
}
