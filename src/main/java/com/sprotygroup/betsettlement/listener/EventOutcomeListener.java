package com.sprotygroup.betsettlement.listener;

import com.sprotygroup.betsettlement.event.EventOutcome;
import com.sprotygroup.betsettlement.service.BetSettlementService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class EventOutcomeListener {
    private final BetSettlementService betSettlementService;

    @KafkaListener(topics = "event-outcomes", groupId = "bet-settlement-group")
    public void handle(@Payload EventOutcome outcome, Acknowledgment acknowledgment) {
        try {
            log.info("Processing message: eventId={}", outcome.eventId());

            Runnable onSuccess = () -> {
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                    log.info("Successfully processed and acknowledged message");
                }
            };

            betSettlementService.settle(outcome, onSuccess);

        } catch (Exception e) {
            log.error("Failed to process message", e);
            throw e;
        }
    }
}
