package com.sprotygroup.betsettlement.listener;

import com.sprotygroup.betsettlement.event.EventOutcome;
import com.sprotygroup.betsettlement.service.BetSettlementService;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EventOutcomeListener {
    private final BetSettlementService betSettlementService;

    @KafkaListener(topics = "event-outcomes", groupId = "bet-settlement-group")
    public void handle(EventOutcome outcome) {
        betSettlementService.settle(outcome);
    }
}
