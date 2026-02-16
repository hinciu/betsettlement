package com.sprotygroup.betsettlement.producer;

import com.sprotygroup.betsettlement.config.RocketMQProperties;
import com.sprotygroup.betsettlement.event.BetSettlement;
import com.sprotygroup.betsettlement.event.EventType;
import com.sprotygroup.betsettlement.exception.SettlementException;
import com.sprotygroup.betsettlement.mapper.BetSettlementMapper;
import com.sprotygroup.betsettlement.model.Bet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementProducer {

    private static final String KEY_TAGS = "rocketmq_TAGS";
    private static final String VALUE_TAGS = "rocketmq_VALUES";

    private final RocketMQTemplate rocketMQTemplate;
    private final RocketMQProperties properties;
    private final BetSettlementMapper betSettlementMapper;


    public void sendSettlementTransactional(List<Bet> bets, Runnable dbTransaction) {
        var topic = properties.getTopic().getBetSettlements();

        bets.forEach(bet -> {
            try {
                BetSettlement settlement = betSettlementMapper.toBetSettlement(bet);

                Message<BetSettlement> message = MessageBuilder
                        .withPayload(settlement)
                        .setHeader(KEY_TAGS, bet.getId().toString())
                        .setHeader(VALUE_TAGS, EventType.SETTLEMENT.name())
                        .build();

                rocketMQTemplate.sendMessageInTransaction(topic, message, dbTransaction);

                log.info("Sent bet settlement to RocketMQ (Transactional) - Topic: {}, BetId: {}",
                        topic, bet.getId());

            } catch (Exception e) {
                log.error("Failed to send settlement to RocketMQ for bet {}: {}", bet.getId(), e.getMessage());
                throw new SettlementException("Failed to send settlement", e.getCause().getMessage());
            }
        });
    }
}
