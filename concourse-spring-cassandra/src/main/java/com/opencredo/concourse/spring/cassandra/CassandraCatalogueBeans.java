package com.opencredo.concourse.spring.cassandra;

import com.opencredo.concourse.cassandra.events.CassandraAggregateCatalogue;
import com.datastax.driver.core.Cluster;
import com.opencredo.concourse.domain.events.cataloguing.AggregateCatalogue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.core.CassandraTemplate;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class CassandraCatalogueBeans {

    @Autowired
    private ConcourseCassandraConfiguration configuration;

    @Bean
    @Primary
    public AggregateCatalogue aggregateCatalogue(Cluster cluster) {
        return CassandraAggregateCatalogue.create(
                new CassandraTemplate(cluster.connect(configuration.getKeyspace())),
                configuration.getCatalogueBucketCount());
    }


}
