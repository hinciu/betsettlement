package com.sprotygroup.betsettlement.listener;

import com.sprotygroup.betsettlement.event.EventOutcome;
import com.sprotygroup.betsettlement.service.BetSettlementService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Slf4j
public class EventOutcomeListener {
    private final BetSettlementService betSettlementService;

    @KafkaListener(topics = "event-outcomes", groupId = "bet-settlement-group")
    @Transactional
    public void handle(
            @Payload EventOutcome outcome,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {
        try {
            log.info("Processing message from partition {} offset {} timestamp {}: eventId={}",
                    partition, offset, timestamp, outcome.eventId());

            betSettlementService.settle(outcome);
            acknowledgment.acknowledge();

            log.info("Successfully processed and acknowledged message from partition {} offset {}",
                    partition, offset);

        } catch (Exception e) {
            log.error("Failed to process message from partition {} offset {}: {}",
                    partition, offset, e.getMessage(), e);
            throw e;
        }
    }
}
