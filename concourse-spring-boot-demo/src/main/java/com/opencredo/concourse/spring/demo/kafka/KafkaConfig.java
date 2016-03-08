package com.opencredo.concourse.spring.demo.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties("kafka")
@Profile("kafka")
public class KafkaConfig {

    @NotNull
    private String bootstrapServers;

    private String zookeeperConnect;

    private String brokerId;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getZookeeperConnect() {
        return zookeeperConnect;
    }

    public void setZookeeperConnect(String zookeeperConnect) {
        this.zookeeperConnect = zookeeperConnect;
    }

    public String getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(String brokerId) {
        this.brokerId = brokerId;
    }

    @Override
    public String toString() {
        return "KafkaConfig{" +
                "zookeeperConnect='" + zookeeperConnect + '\'' +
                ", brokerId='" + brokerId + '\'' +
                ", bootstrapServers='" + bootstrapServers + '\'' +
                '}';
    }
}
