package com.opencredo.concourse.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.json.events.EventJson;
import com.opencredo.concourse.domain.time.TimeUUID;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class RedisEventLog implements EventLog {

    public static RedisEventLog create(Jedis jedis, ObjectMapper objectMapper) {
        return new RedisEventLog(jedis, evt -> EventJson.toString(evt, objectMapper));
    }

    private final Jedis jedis;
    private final Function<Event, String> serialiser;

    private RedisEventLog(Jedis jedis, Function<Event, String> serialiser) {
        this.jedis = jedis;
        this.serialiser = serialiser;
    }

    @Override
    public Collection<Event> apply(Collection<Event> events) {
        if (events.size() == 0) {
            return events;
        }

        if (events.size() == 1) {
            return writeSingleEvent(events.iterator().next());
        }

        return writeMultipleEvents(events);
    }

    private Collection<Event> writeSingleEvent(Event event) {
        Event processedEvent = event.processed(TimeUUID.timeBased());
        jedis.sadd(event.getAggregateId().toString(), serialiser.apply(processedEvent));
        return Collections.singletonList(processedEvent);
    }

    private Collection<Event> writeMultipleEvents(Collection<Event> events) {
        Map<AggregateId, List<Event>> eventsById = events.stream()
                .map(event -> event.processed(TimeUUID.timeBased()))
                .collect(groupingBy(Event::getAggregateId));

        if (eventsById.size() == 1) {
            eventsById.forEach(this::writeEventsForId);
        } else {
            final Pipeline pipeline = jedis.pipelined();

            eventsById.forEach((id, eventsForId) ->
                    writeEventsForId(pipeline, id, eventsForId));

            pipeline.sync();
        }

        return eventsById.values().stream().flatMap(List::stream).collect(toList());
    }

    private Response<Long> writeEventsForId(Pipeline pipeline, AggregateId id, List<Event> eventsForId) {
        return pipeline.sadd(id.toString(), serialiseEvents(eventsForId));
    }

    private long writeEventsForId(AggregateId id, List<Event> eventsForId) {
        return jedis.sadd(id.toString(), serialiseEvents(eventsForId));
    }

    private String[] serialiseEvents(List<Event> eventsForId) {
        return eventsForId.stream().map(serialiser).toArray(String[]::new);
    }
}
