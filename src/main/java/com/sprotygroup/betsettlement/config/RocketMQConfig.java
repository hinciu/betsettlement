package com.sprotygroup.betsettlement.config;

import lombok.AllArgsConstructor;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class RocketMQConfig {

    private final RocketMQProperties properties;

    @Bean(destroyMethod = "shutdown")
    public DefaultMQProducer defaultMQProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(properties.getProducer().getGroup());
        producer.setNamesrvAddr(properties.getNameServer());
        producer.setSendMsgTimeout(properties.getProducer().getSendMessageTimeout());
        producer.setRetryTimesWhenSendFailed(properties.getProducer().getRetryTimesWhenSendFailed());
        producer.setMaxMessageSize(properties.getProducer().getMaxMessageSize());
        producer.start();
        return producer;
    }
}
