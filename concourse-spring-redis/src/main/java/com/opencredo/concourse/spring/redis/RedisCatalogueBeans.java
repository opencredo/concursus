package com.opencredo.concourse.spring.redis;

import com.opencredo.concourse.domain.events.cataloguing.AggregateCatalogue;
import com.opencredo.concourse.redis.RedisAggregateCatalogue;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.Jedis;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class RedisCatalogueBeans {

    @Bean
    @Primary
    public AggregateCatalogue aggregateCatalogue(Jedis jedis) {
        return RedisAggregateCatalogue.create(jedis);
    }

}
