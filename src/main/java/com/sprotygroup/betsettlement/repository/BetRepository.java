package com.sprotygroup.betsettlement.repository;

import com.sprotygroup.betsettlement.model.Bet;
import java.util.stream.Stream;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {

    @Query(value = "SELECT * FROM bets " +
            "WHERE event_id = :eventId " +
            "AND event_winner_id = :winnerId " +
            "AND settled = false " +
            "AND MOD(user_id, :totalBuckets) = :bucketId",
            nativeQuery = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    Stream<Bet> streamWinningBetsByBucket(
            @Param("eventId") Long eventId,
            @Param("winnerId") Long winnerId,
            @Param("totalBuckets") int totalBuckets,
            @Param("bucketId") int bucketId);
}
