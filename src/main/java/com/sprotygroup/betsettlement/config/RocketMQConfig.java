package com.sprotygroup.betsettlement.config;

import com.sprotygroup.betsettlement.producer.BetSettlementTransactionListener;
import lombok.AllArgsConstructor;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@AllArgsConstructor
public class RocketMQConfig {

    private final RocketMQProperties properties;
    private final BetSettlementTransactionListener transactionListener;

    @Bean(destroyMethod = "shutdown")
    public TransactionMQProducer transactionMQProducer() throws MQClientException {
        var producer = new TransactionMQProducer(properties.getProducer().getGroup());
        producer.setNamesrvAddr(properties.getNameServer());
        producer.setSendMsgTimeout(properties.getProducer().getSendMessageTimeout());
        producer.setRetryTimesWhenSendFailed(properties.getProducer().getRetryTimesWhenSendFailed());
        producer.setMaxMessageSize(properties.getProducer().getMaxMessageSize());

        var executor = new ThreadPoolExecutor(
            2,
            5,
            100,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2000),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("bet-settlement-transaction-check-thread");
                return thread;
            }
        );

        producer.setExecutorService(executor);
        producer.setTransactionListener(transactionListener);
        producer.start();
        return producer;
    }
}
