package com.sprotygroup.betsettlement.service;

import com.sprotygroup.betsettlement.dto.SettlementTask;
import com.sprotygroup.betsettlement.event.BetSettlement;
import com.sprotygroup.betsettlement.model.Bet;
import com.sprotygroup.betsettlement.producer.SettlementProducer;
import com.sprotygroup.betsettlement.repository.BetRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class BetSettlementServiceTest {

    @Mock
    private BetRepository betRepository;

    @Mock
    private SettlementProducer settlementProducer;

    @InjectMocks
    private BetSettlementService betSettlementService;

    private SettlementTask task;

    @BeforeEach
    void setUp() {
        task = new SettlementTask(1L, 100L, 0, 10);
    }

    @Test
    void processBucketShouldProcessWinningBets() {
        Bet bet1 = new Bet();
        bet1.setId(10L);
        Bet bet2 = new Bet();
        bet2.setId(20L);

        Stream<Bet> betStream = Stream.of(bet1, bet2);

        given(betRepository.streamWinningBetsByBucket(task.eventId(), task.winnerId(), task.totalBuckets(), task.bucketId()))
                .willReturn(betStream);


        betSettlementService.processBucket(task);

        assertTrue(bet1.isSettled());
        assertTrue(bet2.isSettled());


        verify(settlementProducer).sendBatch(anyList());


        verify(betRepository).saveAll(anyList());
        verify(betRepository).flush();
    }

    @Test
    void processBucketShouldHandleEmptyStream() {

        given(betRepository.streamWinningBetsByBucket(task.eventId(), task.winnerId(), task.totalBuckets(), task.bucketId()))
                .willReturn(Stream.empty());


        betSettlementService.processBucket(task);

        verify(settlementProducer, never()).sendBatch(anyList());
        verify(betRepository, never()).saveAll(anyList());
        verify(betRepository, never()).flush();
    }

    @Test
    void processBucketShouldProcessBatchesIdeally() {
        int totalBets = 550;

        List<Bet> bets = IntStream.range(0, totalBets)
                .mapToObj(i -> {
                    Bet b = new Bet();
                    b.setId((long) i);
                    return b;
                })
                .toList();

        given(betRepository.streamWinningBetsByBucket(task.eventId(), task.winnerId(), task.totalBuckets(), task.bucketId()))
                .willReturn(bets.stream());

        betSettlementService.processBucket(task);

        verify(settlementProducer, times(2)).sendBatch(anyList());


        verify(betRepository, times(2)).flush();
    }
}
