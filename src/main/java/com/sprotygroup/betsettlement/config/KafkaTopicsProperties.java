package com.sprotygroup.betsettlement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.kafka.topics")
@Data
public class KafkaTopicsProperties {
    private Topic eventOutcomes;
    private Topic settlementTasks;

    @Data
    public static class Topic {
        private String name;
        private int partitions;
        private int replicas = 1;
    }
}

