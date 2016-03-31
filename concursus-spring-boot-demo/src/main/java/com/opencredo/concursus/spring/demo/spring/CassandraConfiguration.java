package com.opencredo.concursus.spring.demo.spring;

import com.opencredo.concursus.spring.cassandra.events.CassandraEventStoreBeans;
import com.opencredo.concursus.spring.cassandra.catalogue.CassandraCatalogueBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan(basePackageClasses = { CassandraEventStoreBeans.class, CassandraCatalogueBeans.class })
@Profile("cassandra")
public class CassandraConfiguration {
}
