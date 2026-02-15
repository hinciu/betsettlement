package com.sprotygroup.betsettlement.producer;

import com.sprotygroup.betsettlement.event.EventOutcome;
import com.sprotygroup.betsettlement.service.FailedEventOutcomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventOutcomeProducer {

    private final KafkaTemplate<String, EventOutcome> kafkaTemplate;
    private final FailedEventOutcomeService failedEventOutcomeService;

    public void publish(EventOutcome outcome) {
        // skipping message key in order to evenly distribute events across partitions as events order does not play crucial role.
        var future = kafkaTemplate.sendDefault(outcome);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent outcome for event [{}] to partition [{}] at offset [{}]",
                        outcome.eventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish outcome for event [{}]: {}",
                        outcome.eventId(), ex.getMessage());

                failedEventOutcomeService.saveFailedEvent(
                        outcome,
                        ex.getMessage()
                );
            }
        });
    }

}
