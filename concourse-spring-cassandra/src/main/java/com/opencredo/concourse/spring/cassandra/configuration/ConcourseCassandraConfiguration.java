package com.opencredo.concourse.spring.cassandra.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="concourse.cassandra")
public class ConcourseCassandraConfiguration {

    private String keyspace = "Concourse";
    private int catalogueBucketCount = 8;

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public int getCatalogueBucketCount() {
        return catalogueBucketCount;
    }

    public void setCatalogueBucketCount(int catalogueBucketCount) {
        this.catalogueBucketCount = catalogueBucketCount;
    }
}
