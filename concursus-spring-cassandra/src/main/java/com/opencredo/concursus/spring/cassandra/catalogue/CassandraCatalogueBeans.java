package com.opencredo.concursus.spring.cassandra.catalogue;

import com.datastax.driver.core.Cluster;
import com.opencredo.concursus.cassandra.events.CassandraAggregateCatalogue;
import com.opencredo.concursus.domain.events.cataloguing.AggregateCatalogue;
import com.opencredo.concursus.spring.cassandra.configuration.ConcursusCassandraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.core.CassandraTemplate;

@Configuration
@ComponentScan(basePackageClasses = ConcursusCassandraConfiguration.class)
public class CassandraCatalogueBeans {

    @Autowired
    private ConcursusCassandraConfiguration configuration;

    @Bean
    @Primary
    public AggregateCatalogue aggregateCatalogue(Cluster cluster) {
        return CassandraAggregateCatalogue.create(
                new CassandraTemplate(cluster.connect(configuration.getKeyspace())),
                configuration.getCatalogueBucketCount());
    }


}
