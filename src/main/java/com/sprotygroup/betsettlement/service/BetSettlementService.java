package com.sprotygroup.betsettlement.service;

import com.sprotygroup.betsettlement.event.BetSettlement;
import com.sprotygroup.betsettlement.mapper.BetSettlementMapper;
import com.sprotygroup.betsettlement.model.Bet;
import com.sprotygroup.betsettlement.event.EventOutcome;
import com.sprotygroup.betsettlement.producer.SettlementProducer;
import com.sprotygroup.betsettlement.repository.BetRepository;

import java.util.List;
import java.util.stream.Collectors;

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
    private final BetSettlementMapper betSettlementMapper;

    @Transactional
    public List<BetSettlement> settle(EventOutcome outcome) {
        List<Bet> bets = betRepository.findAllByEventIdAndEventWinnerIdAndSettled(
                outcome.eventId(),
                outcome.eventWinnerId(),
                false
        );

        if (bets.isEmpty()) {
            log.info("No unsettled bets found for event {} and winner {}", outcome.eventId(), outcome.eventWinnerId());
            return List.of();
        }

        bets.forEach(bet -> bet.setSettled(true));

        Runnable dbTransaction = () -> {
            betRepository.saveAll(bets);
            log.info("Saved {} settled bets for event {}", bets.size(), outcome.eventId());

        };

        settlementProducer.sendSettlementTransactional(bets, dbTransaction);

        return bets.stream()
                .map(betSettlementMapper::toBetSettlement)
                .collect(Collectors.toList());
    }
}
