package com.opencredo.concourse.spring.cassandra.catalogue;

import com.datastax.driver.core.Cluster;
import com.opencredo.concourse.cassandra.events.CassandraAggregateCatalogue;
import com.opencredo.concourse.domain.events.cataloguing.AggregateCatalogue;
import com.opencredo.concourse.spring.cassandra.configuration.ConcourseCassandraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.core.CassandraTemplate;

@Configuration
@ComponentScan(basePackageClasses = ConcourseCassandraConfiguration.class)
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
