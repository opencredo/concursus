package com.opencredo.concursus.spring.cassandra.events;

import com.datastax.driver.core.Cluster;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.cassandra.events.CassandraEventStore;
import com.opencredo.concursus.domain.storing.EventStore;
import com.opencredo.concursus.spring.cassandra.configuration.ConcursusCassandraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.core.CassandraTemplate;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = ConcursusCassandraConfiguration.class)
public class CassandraEventStoreBeans {

    @Autowired
    private ConcursusCassandraConfiguration configuration;

    @Bean
    @Primary
    public EventStore eventStore(Cluster cluster, ObjectMapper objectMapper) {
        return CassandraEventStore.create(new CassandraTemplate(cluster.connect(configuration.getKeyspace())), objectMapper);
    }

}
