package com.opencredo.concursus.spring.redis.catalogue;

import com.opencredo.concursus.domain.events.cataloguing.AggregateCatalogue;
import com.opencredo.concursus.redis.RedisAggregateCatalogue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.Jedis;

@Configuration
public class RedisCatalogueBeans {

    @Bean
    @Primary
    public AggregateCatalogue aggregateCatalogue(Jedis jedis) {
        return RedisAggregateCatalogue.create(jedis);
    }

}
