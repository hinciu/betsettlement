package com.sprotygroup.betsettlement.event;

import java.math.BigDecimal;

public record BetSettlement(Long betId, Long userId, Long eventId, Long eventMarketId, Long eventWinnerId, BigDecimal betAmount) {
}
