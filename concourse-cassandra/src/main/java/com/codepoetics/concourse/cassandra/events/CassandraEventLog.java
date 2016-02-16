package com.codepoetics.concourse.cassandra.events;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.consuming.EventLog;
import com.opencredo.concourse.domain.time.TimeUUID;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

public class CassandraEventLog implements EventLog {

    public static CassandraEventLog create(CassandraTemplate cassandraTemplate, Function<Object, String> serialiser) {
        return new CassandraEventLog(cassandraTemplate, prepareInsert(cassandraTemplate.getSession()), serialiser);
    }

    private static PreparedStatement prepareInsert(Session session) {
        return session.prepare(
                "INSERT INTO Event (aggregateType, aggregateId, eventTimestamp, streamId, processingId, name, version, parameters) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
    }

    private final CassandraTemplate cassandraTemplate;
    private final PreparedStatement preparedStatement;
    private final Function<Object, String> serialiser;

    public CassandraEventLog(CassandraTemplate cassandraTemplate, PreparedStatement preparedStatement, Function<Object, String> serialiser) {
        this.cassandraTemplate = cassandraTemplate;
        this.preparedStatement = preparedStatement;
        this.serialiser = serialiser;
    }

    @Override
    public Collection<Event> apply(Collection<Event> events) {
        if (events.isEmpty()) {
            return events;
        }

        if (events.size() == 1) {
            return persistSingleEvent(events.iterator().next());
        }

        return persistBatch(events);
    }

    private Collection<Event> persistSingleEvent(Event singleEvent) {
        UUID processingId = TimeUUID.timeBased();
        cassandraTemplate.execute(preparedStatement.bind(
                getBindArguments(singleEvent, processingId)
        ));
        return Collections.singleton(singleEvent.processed(processingId));
    }

    private Collection<Event> persistBatch(Collection<Event> events) {
        Collection<Event> processedEvents = new ArrayList<>();

        BatchStatement batch = new BatchStatement();

        events.forEach(event -> {
            UUID processingId = TimeUUID.timeBased();
            batch.add(preparedStatement.bind(getBindArguments(event, processingId)));
            processedEvents.add(event.processed(processingId));
        });

        cassandraTemplate.execute(batch);
        return processedEvents;
    }

    private Object[] getBindArguments(Event event, UUID processingId) {
        return new Object[] {
                event.getAggregateId().getType(),
                event.getAggregateId().getId(),
                Date.from(event.getEventTimestamp().getTimestamp()),
                event.getEventTimestamp().getStreamId(),
                processingId,
                event.getEventName().getName(),
                event.getEventName().getVersion(),
                event.getParameters().serialise(serialiser)
        };
    }

}
