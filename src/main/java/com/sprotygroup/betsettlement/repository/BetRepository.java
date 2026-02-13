package com.sprotygroup.betsettlement.repository;

import com.sprotygroup.betsettlement.model.Bet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetRepository extends JpaRepository<Bet, Long> {
    List<Bet> findByEventIdAndEventWinnerId(Long eventId, Long eventWinnerId);
}
