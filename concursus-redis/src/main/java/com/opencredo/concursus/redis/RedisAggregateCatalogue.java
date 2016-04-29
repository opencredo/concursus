package com.opencredo.concursus.redis;

import com.opencredo.concursus.domain.events.cataloguing.AggregateCatalogue;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.stream.Collectors;

public final class RedisAggregateCatalogue implements AggregateCatalogue {

    public static RedisAggregateCatalogue create(Jedis jedis) {
        return new RedisAggregateCatalogue(jedis);
    }

    private final Jedis jedis;

    private RedisAggregateCatalogue(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public void add(String aggregateType, String aggregateId) {
        jedis.sadd(aggregateType, aggregateId);
    }

    @Override
    public void remove(String aggregateType, String aggregateId) {
        jedis.srem(aggregateType, aggregateId);
    }

    @Override
    public List<String> getAggregateIds(String aggregateType) {
        return jedis.smembers(aggregateType).stream().collect(Collectors.toList());
    }
}
