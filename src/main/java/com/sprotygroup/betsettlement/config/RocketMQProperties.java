package com.sprotygroup.betsettlement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rocketmq")
@Data
public class RocketMQProperties {
    private String nameServer;
    private Producer producer;
    private Topic topic;

    @Data
    public static class Producer {
        private String group;
        private Integer sendMessageTimeout;
        private Integer retryTimesWhenSendFailed;
        private Integer maxMessageSize;
    }

    @Data
    public static class Topic {
        private String betSettlements;
    }
}
