package com.opencredo.concursus.spring.redis.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.domain.events.sourcing.EventRetriever;
import com.opencredo.concursus.redis.RedisEventLog;
import com.opencredo.concursus.redis.RedisEventRetriever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.Jedis;

@Configuration
public class RedisEventStoreBeans {

    @Bean
    public EventRetriever eventRetriever(Jedis jedis, ObjectMapper objectMapper) {
        return RedisEventRetriever.create(jedis, objectMapper);
    }

    @Bean
    @Primary
    public EventLog eventLog(Jedis jedis, ObjectMapper objectMapper) {
        return RedisEventLog.create(jedis, objectMapper);
    }


}
