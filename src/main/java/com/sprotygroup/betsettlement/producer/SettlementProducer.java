package com.sprotygroup.betsettlement.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprotygroup.betsettlement.config.RocketMQProperties;
import com.sprotygroup.betsettlement.event.BetSettlement;
import com.sprotygroup.betsettlement.exception.SettlementException;
import com.sprotygroup.betsettlement.mapper.BetSettlementMapper;
import com.sprotygroup.betsettlement.model.Bet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.sprotygroup.betsettlement.event.EventType.SETTLEMENT;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementProducer {

    private final TransactionMQProducer producer;
    private final RocketMQProperties properties;
    private final ObjectMapper objectMapper;
    private final BetSettlementMapper betSettlementMapper;

    public void sendSettlementTransactional(List<Bet> bets, Runnable dbTransaction) {
        var topic = properties.getTopic().getBetSettlements();

        bets.forEach(bet -> {
            try {
                var settlement = betSettlementMapper.toBetSettlement(bet);

                var messageBody = objectMapper.writeValueAsString(settlement);

                var message = new Message(
                        topic,
                        SETTLEMENT.toString(),
                        bet.getId().toString(),
                        messageBody.getBytes(StandardCharsets.UTF_8)
                );

                var sendResult = producer.sendMessageInTransaction(message, dbTransaction);

                log.info("Sent bet settlement to RocketMQ (Transactional) - Topic: {}, BetId: {}, MsgId: {}, LocalTxState: {}",
                        topic, bet.getId(), sendResult.getMsgId(), sendResult.getLocalTransactionState());

            } catch (Exception e) {
                log.error("Failed to send settlement to RocketMQ for bet {}: {}", bet.getId(), e.getMessage());
                throw new SettlementException("Failed to send settlement", e.getCause().getMessage());
            }
        });
    }
}
