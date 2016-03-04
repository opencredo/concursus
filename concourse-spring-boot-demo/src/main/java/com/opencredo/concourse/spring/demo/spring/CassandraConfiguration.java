package com.opencredo.concourse.spring.demo.spring;

import com.opencredo.concourse.spring.cassandra.events.CassandraEventStoreBeans;
import com.opencredo.concourse.spring.cassandra.catalogue.CassandraCatalogueBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan(basePackageClasses = { CassandraEventStoreBeans.class, CassandraCatalogueBeans.class })
@Profile("cassandra")
public class CassandraConfiguration {
}
