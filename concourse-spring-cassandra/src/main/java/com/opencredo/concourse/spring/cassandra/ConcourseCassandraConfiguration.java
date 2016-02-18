package com.opencredo.concourse.spring.cassandra;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="concourse.cassandra")
public class ConcourseCassandraConfiguration {

    private String keyspace = "Concourse";

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }
}
