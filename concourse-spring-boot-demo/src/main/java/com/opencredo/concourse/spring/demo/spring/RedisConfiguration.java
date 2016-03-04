package com.opencredo.concourse.spring.demo.spring;

import com.opencredo.concourse.spring.redis.RedisBeans;
import com.opencredo.concourse.spring.redis.RedisCatalogueBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.clients.jedis.Jedis;

@Configuration
@ComponentScan(basePackageClasses = { RedisBeans.class, RedisCatalogueBeans.class })
@Profile("redis")
public class RedisConfiguration {

    @Bean
    public Jedis jedis() {
        return new Jedis();
    }
}