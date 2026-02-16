package com.sprotygroup.betsettlement.repository;

import com.sprotygroup.betsettlement.model.Bet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {
    List<Bet> findAllByEventIdAndEventWinnerIdAndSettled(Long eventId, Long eventWinnerId, boolean settled);
}
