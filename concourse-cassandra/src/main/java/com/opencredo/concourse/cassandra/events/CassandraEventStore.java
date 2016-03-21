package com.opencredo.concourse.cassandra.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.storing.ComposedEventStore;
import com.opencredo.concourse.domain.storing.EventStore;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utility class that creates an {@link com.opencredo.concourse.domain.events.channels.EventsOutChannel}
 * and an {@link com.opencredo.concourse.domain.events.sourcing.EventRetriever} for Cassandra,
 * and uses them to build an {@link EventStore}.
 */
public final class CassandraEventStore {

    private CassandraEventStore() {
    }

    /**
     * Create an {@link EventStore} that persists and retrieves {@link Event}s using Cassandra.
     * @param cassandraTemplate The {@link CassandraTemplate} to use to perform queries against Cassandra.
     * @param objectMapper the {@link ObjectMapper} to use to serialise and deserialise event data.
     * @return The constructed {@link EventStore}
     */
    public static EventStore create(CassandraTemplate cassandraTemplate, ObjectMapper objectMapper) {
        return ComposedEventStore.create(
                CassandraEventPersister.create(cassandraTemplate, objectMapper),
                CassandraEventRetriever.create(cassandraTemplate, objectMapper)
        );
    }

    /**
     * Create an {@link EventStore} that persists and retrieves {@link Event}s using Cassandra.
     * @param cassandraTemplate The {@link CassandraTemplate} to use to perform queries against Cassandra.
     * @param serialiser The serialiser to use to serialise {@link com.opencredo.concourse.data.tuples.Tuple} data.
     * @param deserialiser The deserialiser to use to deserialise {@link com.opencredo.concourse.data.tuples.Tuple} data.
     * @return The constructed {@link EventStore}
     */
    public static EventStore create(CassandraTemplate cassandraTemplate, Function<Object, String> serialiser, BiFunction<String, Type, Object> deserialiser) {
        return ComposedEventStore.create(
                CassandraEventPersister.create(cassandraTemplate, serialiser),
                CassandraEventRetriever.create(cassandraTemplate, deserialiser)
        );
    }

}
