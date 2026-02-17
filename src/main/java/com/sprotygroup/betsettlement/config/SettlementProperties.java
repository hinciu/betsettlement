package com.sprotygroup.betsettlement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.settlement")
public class SettlementProperties {
    private int totalBuckets = 64;
    private String taskTopic = "settlement-tasks";
    private String group;
}

