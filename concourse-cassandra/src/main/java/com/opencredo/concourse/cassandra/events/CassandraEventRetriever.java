package com.opencredo.concourse.cassandra.events;

import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.EventRetriever;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.domain.time.TimeRangeBound;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * An {@link EventRetriever} that retrieves events from Cassandra.
 */
public final class CassandraEventRetriever implements EventRetriever {

    /**
     * Construct an {@link EventRetriever} that retrieves events from Cassandra using the supplied
     * {@link CassandraTemplate} and {@link ObjectMapper}
     * @param cassandraTemplate The {@link CassandraTemplate} to use to execute Cassandra queries.
     * @param objectMapper The {@link ObjectMapper} to use to deserialise Event data.
     * @return The constructed {@link EventRetriever}.
     */
    public static EventRetriever create(CassandraTemplate cassandraTemplate, ObjectMapper objectMapper) {
        return create(cassandraTemplate, JsonDeserialiser.using(objectMapper));
    }

    /**
     * Construct an {@link EventRetriever} that retrieves events from Cassandra using the supplied
     * {@link CassandraTemplate} and deserialising {@link BiFunction}.
     * @param cassandraTemplate The {@link CassandraTemplate} to use to execute Cassandra queries.
     * @param deserialiser The deserialiser to use to deserialise Event data.
     * @return The constructed {@link EventRetriever}.
     */
    public static EventRetriever create(CassandraTemplate cassandraTemplate, BiFunction<String, Type, Object> deserialiser) {
        return new CassandraEventRetriever(cassandraTemplate, deserialiser);
    }

    private final CassandraTemplate cassandraTemplate;
    private final BiFunction<String, Type, Object> deserialiser;

    private CassandraEventRetriever(CassandraTemplate cassandraTemplate, BiFunction<String, Type, Object> deserialiser) {
        this.cassandraTemplate = cassandraTemplate;
        this.deserialiser = deserialiser;
    }

    @Override
    public List<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange) {
        final Select select = selectFromEvent();

        select.where(QueryBuilder.eq("aggregateType", aggregateId.getType()))
                .and(QueryBuilder.eq("aggregateId", aggregateId.getId()));

        constrainTimeRange(timeRange, select);

        List<Event> results = new LinkedList<>();

        runAndTranslate(matcher, select, results::add);

        return results;
    }

    @Override
    public Map<AggregateId, List<Event>> getEvents(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
        final Select select = selectFromEvent();

        select.where(QueryBuilder.eq("aggregateType", aggregateType))
                .and(QueryBuilder.in("aggregateId", aggregateIds.stream().collect(Collectors.toList())));

        constrainTimeRange(timeRange, select);

        Map<AggregateId, List<Event>> results = new HashMap<>();
        Consumer<Event> addToResults = event -> results.computeIfAbsent(event.getAggregateId(), id -> new LinkedList<>()).add(event);

        runAndTranslate(matcher, select, addToResults);

        return results;
    }

    private Consumer<TimeRangeBound> constrainUpperBound(Select select) {
        return constrainBound(select, QueryBuilder::lte, QueryBuilder::lt);
    }

    private Consumer<TimeRangeBound> constrainLowerBound(Select select) {
        return constrainBound(select, QueryBuilder::gte, QueryBuilder::gt);
    }

    private Consumer<TimeRangeBound> constrainBound(
            Select select,
            BiFunction<String, Long, Clause> inclusive,
            BiFunction<String, Long, Clause> exclusive) {
        return bound -> select.where((bound.isInclusive() ? inclusive : exclusive).apply("eventTimestamp", bound.getInstant().toEpochMilli()));
    }

    private void runAndTranslate(EventTypeMatcher matcher, Select select, Consumer<Event> addToResults) {
        EventTranslator eventTranslator = EventTranslator.using(matcher, deserialiser, addToResults);
        cassandraTemplate.query(select, eventTranslator);
    }

    private void constrainTimeRange(TimeRange timeRange, Select select) {
        timeRange.getUpperBound().ifPresent(constrainUpperBound(select));
        timeRange.getLowerBound().ifPresent(constrainLowerBound(select));
    }

    private Select selectFromEvent() {
        return QueryBuilder.select(
                "aggregateType", "aggregateId", "eventTimestamp", "streamId",
                "processingId", "name", "version", "parameters", "characteristics")
                .from("Event");
    }
}
