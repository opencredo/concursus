package com.opencredo.concourse.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.EventRetriever;
import com.opencredo.concourse.domain.events.matching.EventTypeMatcher;
import com.opencredo.concourse.domain.json.events.EventJson;
import com.opencredo.concourse.domain.time.TimeRange;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class RedisEventRetriever implements EventRetriever {

    public static RedisEventRetriever create(Jedis jedis, ObjectMapper objectMapper) {
        return new RedisEventRetriever(jedis, objectMapper);
    }

    private final Jedis jedis;
    private final ObjectMapper objectMapper;

    private RedisEventRetriever(Jedis jedis, ObjectMapper objectMapper) {
        this.jedis = jedis;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Event> getEvents(EventTypeMatcher matcher, AggregateId aggregateId, TimeRange timeRange) {
        Function<String, Optional<Event>> deserialiser = eventJson -> EventJson.fromString(eventJson, matcher, objectMapper);

        final Set<String> eventsForId = jedis.smembers(aggregateId.toString());
        return deserialiseAll(timeRange, deserialiser, eventsForId);
    }

    private List<Event> deserialiseAll(TimeRange timeRange, Function<String, Optional<Event>> deserialiser, Set<String> eventsForId) {
        return eventsForId.stream()
                .map(deserialiser::apply)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(event -> timeRange.contains(event.getEventTimestamp().getTimestamp()))
                .sorted(comparing(Event::getEventTimestamp).reversed())
                .collect(toList());
    }

    @Override
    public Map<AggregateId, List<Event>> getEvents(EventTypeMatcher matcher, String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange) {
        Function<String, Optional<Event>> deserialiser = eventJson -> EventJson.fromString(eventJson, matcher, objectMapper);
        Pipeline pipeline = jedis.pipelined();

        final Map<AggregateId, Response<Set<String>>> responses = aggregateIds.stream()
                .map(id -> AggregateId.of(aggregateType, id))
                .collect(toMap(
                        Function.identity(),
                        id -> pipeline.smembers(id.toString())));

        pipeline.sync();

        return responses.entrySet().stream()
                .collect(toMap(
                        Entry::getKey,
                        e -> deserialiseAll(timeRange, deserialiser, e.getValue().get())));
    }
}
