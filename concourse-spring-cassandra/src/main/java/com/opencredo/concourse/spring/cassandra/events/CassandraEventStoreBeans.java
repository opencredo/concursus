package com.opencredo.concourse.spring.cassandra.events;

import com.datastax.driver.core.Cluster;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.cassandra.events.CassandraEventLog;
import com.opencredo.concourse.cassandra.events.CassandraEventRetriever;
import com.opencredo.concourse.cassandra.events.JsonDeserialiser;
import com.opencredo.concourse.cassandra.events.JsonSerialiser;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.events.sourcing.EventRetriever;
import com.opencredo.concourse.spring.cassandra.configuration.ConcourseCassandraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.core.CassandraTemplate;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = ConcourseCassandraConfiguration.class)
public class CassandraEventStoreBeans {

    @Autowired
    private ConcourseCassandraConfiguration configuration;

    @Bean
    public JsonDeserialiser deserialiser(ObjectMapper mapper) {
        return JsonDeserialiser.using(mapper);
    }

    @Bean
    public JsonSerialiser serialiser(ObjectMapper mapper) {
        return JsonSerialiser.using(mapper);
    }

    @Bean
    public EventRetriever eventRetriever(Cluster cluster, JsonDeserialiser deserialiser) {
        return CassandraEventRetriever.create(
                new CassandraTemplate(cluster.connect(configuration.getKeyspace())),
                deserialiser);
    }

    @Bean
    @Primary
    public EventLog eventLog(Cluster cluster, JsonSerialiser serialiser) {
        return CassandraEventLog.create(
                new CassandraTemplate(cluster.connect(configuration.getKeyspace())),
                serialiser);
    }


}
