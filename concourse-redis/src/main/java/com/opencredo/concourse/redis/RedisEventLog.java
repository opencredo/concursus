package com.opencredo.concourse.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.json.EventJson;
import com.opencredo.concourse.domain.time.TimeUUID;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class RedisEventLog implements EventLog {

    public static RedisEventLog using(Jedis jedis, ObjectMapper objectMapper) {
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
        Map<AggregateId, List<Event>> eventsById = events.stream()
                .map(event -> event.processed(TimeUUID.timeBased()))
                .collect(groupingBy(Event::getAggregateId));

        eventsById.forEach((id, eventsForId) ->
                jedis.sadd(id.toString(), eventsForId.stream().map(serialiser).toArray(String[]::new)));

        return eventsById.values().stream().flatMap(List::stream).collect(toList());
    }
}
