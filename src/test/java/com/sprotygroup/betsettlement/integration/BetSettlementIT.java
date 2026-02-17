package com.sprotygroup.betsettlement.integration;

import com.sprotygroup.betsettlement.model.Bet;
import com.sprotygroup.betsettlement.repository.BetRepository;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sprotygroup.betsettlement.controller.EventOutcomeController.BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class BetSettlementIT extends BaseIT {
    @Autowired
    private BetRepository betRepository;

    @MockitoBean
    private RocketMQTemplate rocketMQTemplate;

    private DefaultMQProducer mockProducer;

    @BeforeEach
    void setUp() {
        mockProducer = mock(DefaultMQProducer.class);
        when(rocketMQTemplate.getProducer()).thenReturn(mockProducer);
    }

    @Test
    @Sql(scripts = "classpath:sql/clear-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    @Sql("classpath:sql/insert-bets.sql")
    void shouldSettleWinningBetsWhenEventOutcomeReceived() throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readMockRequest("success-event")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId").value(5001L))
                .andExpect(jsonPath("$.eventName").value("Championship Final"))
                .andExpect(jsonPath("$.eventWinnerId").value(9001L));

        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(mockProducer, times(2)).send(anyList());

                    List<Bet> allBets = betRepository.findAll();

                    Bet winningBet1 = allBets.stream()
                            .filter(b -> b.getId().equals(1L))
                            .findFirst()
                            .orElseThrow();
                    assertThat(winningBet1.isSettled()).isTrue();
                    assertThat(winningBet1.getEventWinnerId()).isEqualTo(9001L);

                    Bet losingBet = allBets.stream()
                            .filter(b -> b.getId().equals(2L))
                            .findFirst()
                            .orElseThrow();
                    assertThat(losingBet.isSettled()).isFalse();
                    assertThat(losingBet.getEventWinnerId()).isEqualTo(9002L);

                    Bet winningBet2 = allBets.stream()
                            .filter(b -> b.getId().equals(4L))
                            .findFirst()
                            .orElseThrow();
                    assertThat(winningBet2.isSettled()).isTrue();
                    assertThat(winningBet2.getEventWinnerId()).isEqualTo(9001L);
                });
    }

    @Test
    @Sql(scripts = "classpath:sql/clear-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    @Sql("classpath:sql/insert-already-settled-bets.sql")
    void shouldNotSettleAlreadySettledBets() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readMockRequest("success-event")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId").value(5001L))
                .andExpect(jsonPath("$.eventName").value("Championship Final"))
                .andExpect(jsonPath("$.eventWinnerId").value(9001L));

        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<Bet> allBets = betRepository.findAll();

                    long settledCount = allBets.stream()
                            .filter(b -> b.getEventId().equals(5001L))
                            .filter(Bet::isSettled)
                            .count();
                    assertThat(settledCount).isEqualTo(2);

                    Bet alreadySettledBet1 = allBets.stream()
                            .filter(b -> b.getId().equals(1L))
                            .findFirst()
                            .orElseThrow();
                    assertThat(alreadySettledBet1.isSettled()).isTrue();

                    Bet alreadySettledBet2 = allBets.stream()
                            .filter(b -> b.getId().equals(3L))
                            .findFirst()
                            .orElseThrow();
                    assertThat(alreadySettledBet2.isSettled()).isTrue();

                    Bet losingBet = allBets.stream()
                            .filter(b -> b.getId().equals(2L))
                            .findFirst()
                            .orElseThrow();
                    assertThat(losingBet.isSettled()).isFalse();
                });

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readMockRequest("success-event")))
                .andExpect(status().isAccepted());

        await()
                .pollDelay(2, TimeUnit.SECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<Bet> allBets = betRepository.findAll();

                    long settledCount = allBets.stream()
                            .filter(b -> b.getEventId().equals(5001L))
                            .filter(Bet::isSettled)
                            .count();
                    assertThat(settledCount).isEqualTo(2);
                });
    }

    @Test
    @Sql(scripts = "classpath:sql/clear-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    @Sql("classpath:sql/insert-different-event-bets.sql")
    void shouldHandleEventWithNoMatchingBets() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readMockRequest("no-matching-bets-event")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId").value(9999L))
                .andExpect(jsonPath("$.eventName").value("Non-existent Event"))
                .andExpect(jsonPath("$.eventWinnerId").value(1L));

        await()
                .pollDelay(2, TimeUnit.SECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<Bet> allBets = betRepository.findAll();
                    assertThat(allBets).hasSize(3);
                    assertThat(allBets).noneMatch(Bet::isSettled);
                });
    }

    @Test
    @Sql(scripts = "classpath:sql/clear-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    @Sql("classpath:sql/insert-bets-for-duplicate.sql")
    void shouldSettleBetsOnlyOnceForDuplicateEventRequests() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readMockRequest("duplicate-event")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId").value(6001L))
                .andExpect(jsonPath("$.eventName").value("Duplicate Event Test"))
                .andExpect(jsonPath("$.eventWinnerId").value(9100L));

        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(mockProducer, times(2)).send(anyList()); // Assuming distribution similar to success test

                    List<Bet> allBets = betRepository.findAll();
                    assertThat(allBets).hasSize(3);

                    long settledCount = allBets.stream()
                            .filter(Bet::isSettled)
                            .count();
                    assertThat(settledCount).isEqualTo(2);
                });

        clearInvocations(mockProducer);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readMockRequest("duplicate-event")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId").value(6001L));

        await()
                .pollDelay(2, TimeUnit.SECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verifyNoInteractions(mockProducer);

                    List<Bet> allBets = betRepository.findAll();
                    assertThat(allBets).hasSize(3);

                    long settledCount = allBets.stream()
                            .filter(Bet::isSettled)
                            .count();
                    assertThat(settledCount).isEqualTo(2);
                });
    }

    @Test
    @Sql(scripts = "classpath:sql/clear-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    @Sql("classpath:sql/insert-bets-for-rollback.sql")
    void shouldRollbackSettledBetsWhenRocketMQPublishFails() throws Exception {
        doThrow(new RuntimeException("RocketMQ publish failed"))
                .when(mockProducer).send(anyList());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readMockRequest("rollback-event")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId").value(7001L))
                .andExpect(jsonPath("$.eventName").value("Rollback Event Test"))
                .andExpect(jsonPath("$.eventWinnerId").value(9300L));

        await()
                .pollDelay(2, TimeUnit.SECONDS) // Wait for processing to fail
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<Bet> allBets = betRepository.findAll();
                    assertThat(allBets).hasSize(2);
                    assertThat(allBets).allMatch(bet -> !bet.isSettled());
                });
    }

}
