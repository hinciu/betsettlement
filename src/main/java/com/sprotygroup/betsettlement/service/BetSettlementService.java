package com.sprotygroup.betsettlement.service;

import com.sprotygroup.betsettlement.model.Bet;
import com.sprotygroup.betsettlement.producer.SettlementProducer;
import com.sprotygroup.betsettlement.repository.BetRepository;
import com.sprotygroup.betsettlement.dto.SettlementTask;
import com.sprotygroup.betsettlement.exception.SettlementException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class BetSettlementService {
    private final BetRepository betRepository;
    private final SettlementProducer settlementProducer;

    @Transactional
    public void processBucket(SettlementTask task) {
        log.info("Starting processing bucket {}/{} for event {}", task.bucketId(), task.totalBuckets(), task.eventId());

        try (Stream<Bet> betStream = betRepository.streamWinningBetsByBucket(
                task.eventId(), task.winnerId(), task.totalBuckets(), task.bucketId())) {

            List<Bet> currentBatch = new ArrayList<>();
            int[] processedCount = {0};

            betStream.forEach(bet -> {
                bet.setSettled(true);
                currentBatch.add(bet);
                processedCount[0]++;

                if (currentBatch.size() >= 500) {
                    processBatch(currentBatch);
                    currentBatch.clear();
                }
            });

            if (!currentBatch.isEmpty()) {
                processBatch(currentBatch);
            }

            log.info("Finished processing bucket {}/{} for event {}. Total processed: {}",
                    task.bucketId(), task.totalBuckets(), task.eventId(), processedCount[0]);

        } catch (Exception e) {
            log.error("Error processing bucket {} for event {}", task.bucketId(), task.eventId(), e);
            throw new SettlementException("Failed to process bucket " + task.bucketId(), e.getMessage());
        }
    }

    private void processBatch(List<Bet> batch) {
        if (batch.isEmpty()) return;
        betRepository.saveAll(batch);
        betRepository.flush();
        settlementProducer.sendBatch(batch);
    }
}
