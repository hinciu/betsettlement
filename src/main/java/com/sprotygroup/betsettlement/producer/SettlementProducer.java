package com.sprotygroup.betsettlement.producer;

import com.sprotygroup.betsettlement.config.RocketMQProperties;
import com.sprotygroup.betsettlement.event.BetSettlement;
import com.sprotygroup.betsettlement.event.EventType;
import com.sprotygroup.betsettlement.exception.SettlementException;
import com.sprotygroup.betsettlement.mapper.BetSettlementMapper;
import com.sprotygroup.betsettlement.model.Bet;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final RocketMQProperties properties;
    private final BetSettlementMapper betSettlementMapper;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void sendBatch(List<Bet> bets) {
        if (bets.isEmpty()) {
            return;
        }
        var topic = properties.getTopic().getBetSettlements();

        List<Message> messages = bets.stream()
                .map(bet -> {
                    try {
                        BetSettlement settlement = betSettlementMapper.toBetSettlement(bet);
                        var message = new Message(
                                topic,
                                objectMapper.writeValueAsBytes(settlement));
                        message.setKeys(bet.getId().toString());
                        message.setTags(EventType.SETTLEMENT.name());
                        return message;
                    } catch (Exception e) {
                        throw new SettlementException("Failed to send batch", e.getMessage());
                    }
                })
                .collect(Collectors.toList());

        try {
            rocketMQTemplate.getProducer().send(messages);

            log.info("Sent batch of {} bet settlements to RocketMQ", bets.size());
        } catch (Exception e) {
            log.error("Failed to send batch of settlements to RocketMQ: {}", e.getMessage(), e);
            throw new SettlementException("Failed to send batch", e.getMessage());
        }
    }
}
