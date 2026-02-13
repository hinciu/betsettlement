package com.sprotygroup.betsettlement.producer;

import com.sprotygroup.betsettlement.model.Bet;
import com.sprotygroup.betsettlement.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

@RocketMQTransactionListener
@RequiredArgsConstructor
@Slf4j
public class BetSettlementTransactionListener implements RocketMQLocalTransactionListener {

    private final BetRepository betRepository;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            log.info("Executing local transaction for message: {}", msg.getHeaders().get("rocketmq_KEYS"));

            if (arg instanceof Runnable) {
                ((Runnable) arg).run();
                log.info("Local transaction committed successfully");
                return RocketMQLocalTransactionState.COMMIT;
            }

            log.warn("Invalid transaction argument type");
            return RocketMQLocalTransactionState.ROLLBACK;

        } catch (Exception e) {
            log.error("Local transaction failed: {}", e.getMessage(), e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        try {
            String betId = (String) msg.getHeaders().get("rocketmq_KEYS");
            log.info("Checking local transaction status for bet: {}", betId);

            boolean isSettled = betRepository.findById(Long.parseLong(betId))
                .map(Bet::isSettled)
                .orElse(false);

            if (isSettled) {
                log.info("Bet {} is settled, committing message", betId);
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                log.info("Bet {} is not settled, rolling back message", betId);
                return RocketMQLocalTransactionState.ROLLBACK;
            }

        } catch (Exception e) {
            log.error("Error checking transaction status: {}", e.getMessage());
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
}
