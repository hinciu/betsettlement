package com.sprotygroup.betsettlement.producer;

import com.sprotygroup.betsettlement.event.BetSettlement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SettlementProducer {
    private static final Logger logger = LoggerFactory.getLogger(SettlementProducer.class);

    public void sendSettlement(List<BetSettlement> settlement) {
        logger.info("bet-settlements payloard: {}", settlement);
    }
}
