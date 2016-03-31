package com.opencredo.concursus.spring.cassandra.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="concursus.cassandra")
public class ConcursusCassandraConfiguration {

    private String keyspace = "concursus";
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
