package com.codepoetics.concourse.cassandra.events;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.DriverException;
import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.VersionedName;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import org.springframework.cassandra.core.RowCallbackHandler;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

final class EventTranslator implements RowCallbackHandler {

    private static final int AGGREGATE_TYPE = 0;
    private static final int AGGREGATE_ID = 1;
    private static final int EVENT_TIMESTAMP = 2;
    private static final int STREAM_ID = 3;
    private static final int EVENT_NAME = 5;
    private static final int EVENT_VERSION = 6;
    private static final int PARAMETERS = 7;

    public static EventTranslator using(EventTypeMatcher matcher, BiFunction<String, Type, Object> deserialiser, Consumer<Event> eventCollector) {
        return new EventTranslator(matcher, deserialiser, eventCollector);
    }

    private final EventTypeMatcher matcher;
    private final BiFunction<String, Type, Object> deserialiser;
    private final Consumer<Event> eventCollector;

    private EventTranslator(EventTypeMatcher matcher, BiFunction<String, Type, Object> deserialiser, Consumer<Event> eventCollector) {
        this.matcher = matcher;
        this.deserialiser = deserialiser;
        this.eventCollector = eventCollector;
    }

    @Override
    public void processRow(Row row) throws DriverException {
        String aggregateType = row.getString(AGGREGATE_TYPE);
        String name = row.getString(EVENT_NAME);
        String version = row.getString(EVENT_VERSION);

        VersionedName versionedName = VersionedName.of(name, version);
        EventType eventType = EventType.of(aggregateType, versionedName);

        matcher.match(eventType).ifPresent(tupleSchema -> {
            createEvent(row, aggregateType, versionedName, tupleSchema);
        });
    }

    private void createEvent(Row row, String aggregateType, VersionedName versionedName, TupleSchema tupleSchema) {
        Map<String, String> parameterData = row.getMap(PARAMETERS, String.class, String.class);
        Tuple parameters = tupleSchema.deserialise(deserialiser, parameterData);

        Event event = Event.of(
                AggregateId.of(aggregateType, row.getUUID(AGGREGATE_ID)),
                StreamTimestamp.of(row.getString(STREAM_ID), row.getDate(EVENT_TIMESTAMP).toInstant()),
                versionedName,
                parameters);

        eventCollector.accept(event);
    }

}
