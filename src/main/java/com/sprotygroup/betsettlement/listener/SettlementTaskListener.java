package com.sprotygroup.betsettlement.listener;

import com.sprotygroup.betsettlement.dto.SettlementTask;
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
public class SettlementTaskListener {
    private final BetSettlementService betSettlementService;

    @KafkaListener(topics = "${app.settlement.task-topic}", groupId = "${app.kafka.topics.settlement-tasks.group}")
    public void handle(
            @Payload SettlementTask task,
            Acknowledgment acknowledgment) {

        try {
            log.info("Processing settlement task for bucket {}/{} eventId={}",
                    task.bucketId(), task.totalBuckets(), task.eventId());

            betSettlementService.processBucket(task);

            acknowledgment.acknowledge();
            log.info("Completed settlement task for bucket {}/{}", task.bucketId(), task.totalBuckets());

        } catch (Exception e) {
            log.error("Failed to process settlement task for bucket {}: {}", task.bucketId(), e.getMessage(), e);
            throw e;
        }
    }
}

