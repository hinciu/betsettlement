package com.sprotygroup.betsettlement.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprotygroup.betsettlement.config.RocketMQProperties;
import com.sprotygroup.betsettlement.event.BetSettlement;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SettlementProducer {
    private static final Logger logger = LoggerFactory.getLogger(SettlementProducer.class);

    private final DefaultMQProducer producer;
    private final RocketMQProperties properties;
    private final ObjectMapper objectMapper;

    public void sendSettlement(List<BetSettlement> settlements) {
        String topic = properties.getTopic().getBetSettlements();

        settlements.forEach(settlement -> {
            try {
                String messageBody = objectMapper.writeValueAsString(settlement);
                Message message = new Message(
                    topic,
                    "SETTLEMENT",
                    settlement.betId().toString(),
                    messageBody.getBytes(StandardCharsets.UTF_8)
                );

                SendResult sendResult = producer.send(message);
                logger.info("Sent bet settlement to RocketMQ - Topic: {}, BetId: {}, MsgId: {}, Status: {}",
                    topic, settlement.betId(), sendResult.getMsgId(), sendResult.getSendStatus());

            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize settlement for bet {}: {}", settlement.betId(), e.getMessage());
            } catch (Exception e) {
                logger.error("Failed to send settlement to RocketMQ for bet {}: {}", settlement.betId(), e.getMessage());
            }
        });
    }
}
