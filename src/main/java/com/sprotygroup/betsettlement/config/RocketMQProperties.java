package com.sprotygroup.betsettlement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rocketmq")
@Data
public class RocketMQProperties {
    private Topic topic;

    @Data
    public static class Topic {
        private String betSettlements;
    }
}
