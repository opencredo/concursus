package com.opencredo.concourse.spring.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.events.caching.CachingEventSource;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.events.sourcing.EventRetriever;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.redis.RedisEventLog;
import com.opencredo.concourse.redis.RedisEventRetriever;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.Jedis;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class RedisBeans {

    @Bean
    public EventRetriever eventRetriever(Jedis jedis, ObjectMapper objectMapper) {
        return RedisEventRetriever.create(jedis, objectMapper);
    }

    @Bean
    @Primary
    public EventSource eventSource(EventRetriever eventRetriever) {
        return CachingEventSource.retrievingWith(eventRetriever);
    }

    @Bean
    @Primary
    public EventLog eventLog(Jedis jedis, ObjectMapper objectMapper) {
        return RedisEventLog.create(jedis, objectMapper);
    }


}
