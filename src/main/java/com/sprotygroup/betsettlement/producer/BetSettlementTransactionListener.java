package com.sprotygroup.betsettlement.producer;

import com.sprotygroup.betsettlement.model.Bet;
import com.sprotygroup.betsettlement.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BetSettlementTransactionListener implements TransactionListener {

    private final BetRepository betRepository;

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            log.info("Executing local transaction for message: {}", msg.getKeys());

            if (arg instanceof Runnable) {
                ((Runnable) arg).run();
                log.info("Local transaction committed successfully for bet: {}", msg.getKeys());
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            log.warn("Invalid transaction argument type for bet: {}", msg.getKeys());
            return LocalTransactionState.ROLLBACK_MESSAGE;

        } catch (Exception e) {
            log.error("Local transaction failed for bet {}: {}", msg.getKeys(), e.getMessage(), e);
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        try {
            var betId = msg.getKeys();
            log.info("Checking local transaction status for bet: {}", betId);

            boolean isSettled = betRepository.findById(Long.parseLong(betId))
                .map(Bet::isSettled)
                .orElse(false);

            if (isSettled) {
                log.info("Bet {} is settled, committing message", betId);
                return LocalTransactionState.COMMIT_MESSAGE;
            } else {
                log.info("Bet {} is not settled, rolling back message", betId);
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }

        } catch (Exception e) {
            log.error("Error checking transaction status for message {}: {}", msg.getKeys(), e.getMessage());
            return LocalTransactionState.UNKNOW;
        }
    }
}
