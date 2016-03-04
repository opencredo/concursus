package com.opencredo.concourse.spring.demo.spring;

import com.opencredo.concourse.spring.cassandra.CassandraBeans;
import com.opencredo.concourse.spring.cassandra.CassandraCatalogueBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan(basePackageClasses = { CassandraBeans.class, CassandraCatalogueBeans.class })
@Profile("cassandra")
public class CassandraConfiguration {
}
