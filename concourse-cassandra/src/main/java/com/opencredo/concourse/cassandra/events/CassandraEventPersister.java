package com.opencredo.concourse.cassandra.events;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.persisting.EventPersister;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.sql.Date;
import java.util.Collection;
import java.util.function.Function;

public final class CassandraEventPersister implements EventPersister {

    public static CassandraEventPersister create(CassandraTemplate cassandraTemplate, Function<Object, String> serialiser) {
        return new CassandraEventPersister(cassandraTemplate, prepareInsert(cassandraTemplate.getSession()), serialiser);
    }

    private static PreparedStatement prepareInsert(Session session) {
        return session.prepare(
                "INSERT INTO Event (aggregateType, aggregateId, eventTimestamp, streamId, processingId, name, version, parameters, characteristics) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    }

    private final CassandraTemplate cassandraTemplate;
    private final PreparedStatement preparedStatement;
    private final Function<Object, String> serialiser;

    public CassandraEventPersister(CassandraTemplate cassandraTemplate, PreparedStatement preparedStatement, Function<Object, String> serialiser) {
        this.cassandraTemplate = cassandraTemplate;
        this.preparedStatement = preparedStatement;
        this.serialiser = serialiser;
    }

    @Override
    public void accept(Collection<Event> events) {
        if (events.size() == 1) {
            persistSingleEvent(events.iterator().next());
        } else if (!events.isEmpty()) {
            persistBatch(events);
        }
    }

    private void persistSingleEvent(Event singleEvent) {
        cassandraTemplate.execute(preparedStatement.bind(getBindArguments(singleEvent)));
    }

    private void persistBatch(Collection<Event> events) {
        BatchStatement batch = new BatchStatement();

        events.stream().map(this::getBindArguments).map(preparedStatement::bind).forEach(batch::add);

        cassandraTemplate.execute(batch);
    }

    private Object[] getBindArguments(Event event) {
        return new Object[] {
                event.getAggregateId().getType(),
                event.getAggregateId().getId(),
                Date.from(event.getEventTimestamp().getTimestamp()),
                event.getEventTimestamp().getStreamId(),
                event.getProcessingId().orElseThrow(() -> new IllegalArgumentException("Event has no processing id")),
                event.getEventName().getName(),
                event.getEventName().getVersion(),
                event.getParameters().serialise(serialiser),
                event.getCharacteristics()
        };
    }

}
