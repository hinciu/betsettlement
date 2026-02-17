package com.sprotygroup.betsettlement.listener;

import com.sprotygroup.betsettlement.config.SettlementProperties;
import com.sprotygroup.betsettlement.event.EventOutcome;
import com.sprotygroup.betsettlement.dto.SettlementTask;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

@Component
@AllArgsConstructor
@Slf4j
public class EventOutcomeListener {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SettlementProperties properties;

    @KafkaListener(topics = "${app.kafka.topics.event-outcomes.name}", groupId = "${app.kafka.topics.event-outcomes.group}")
    @Transactional
    public void handle(
            @Payload EventOutcome outcome,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("Processing event execution request: eventId={}", outcome.eventId());

            IntStream.range(0, properties.getTotalBuckets()).forEach(bucketId -> {
                SettlementTask task = new SettlementTask(
                        outcome.eventId(),
                        outcome.eventWinnerId(),
                        bucketId,
                        properties.getTotalBuckets()
                );

                kafkaTemplate.send(properties.getTaskTopic(), String.valueOf(bucketId), task);
            });

            acknowledgment.acknowledge();
            log.info("Dispatched {} settlement tasks for eventId={}", properties.getTotalBuckets(), outcome.eventId());

        } catch (Exception e) {
            log.error("Failed to process message from partition {} offset {}: {}",
                    partition, offset, e.getMessage(), e);
            throw e;
        }
    }
}
