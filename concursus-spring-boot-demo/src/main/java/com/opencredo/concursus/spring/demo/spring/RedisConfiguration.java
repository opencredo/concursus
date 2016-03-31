package com.opencredo.concursus.spring.demo.spring;

import com.opencredo.concursus.spring.redis.events.RedisEventStoreBeans;
import com.opencredo.concursus.spring.redis.catalogue.RedisCatalogueBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.clients.jedis.Jedis;

@Configuration
@ComponentScan(basePackageClasses = { RedisEventStoreBeans.class, RedisCatalogueBeans.class })
@Profile("redis")
public class RedisConfiguration {

    @Bean
    public Jedis jedis() {
        return new Jedis();
    }
}
