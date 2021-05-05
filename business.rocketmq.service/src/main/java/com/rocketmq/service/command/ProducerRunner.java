package com.rocketmq.service.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * The type Producer runner.
 */
@Component
public class ProducerRunner implements CommandLineRunner {

    // 声明并引用RocketMQTemplate
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.topic}")
    private String topic;

    /**
     * Callback used to run the bean.
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        // 以同步的方式发送字符串消息给指定的topic
        SendResult sendResult = rocketMQTemplate.syncSend(topic, "Hello, World!");
        // 打印发送结果信息
        System.out.printf("string-topic syncSend1 sendResult=%s %n", sendResult);

        //send message synchronously
        rocketMQTemplate.convertAndSend("test-topic-1", "Hello, World!");
        //send spring message
        rocketMQTemplate.send("test-topic-1", MessageBuilder.withPayload("Hello, World! I'm from spring message").build());
        //send messgae asynchronously
        rocketMQTemplate.asyncSend("test-topic-2", new OrderPaidEvent("T_001", new BigDecimal("88.00")), new SendCallback() {
            @Override
            public void onSuccess(SendResult var1) {
                System.out.printf("async onSucess SendResult=%s %n", var1);
            }

            @Override
            public void onException(Throwable var1) {
                System.out.printf("async onException Throwable=%s %n", var1);
            }

        });
        //Send messages orderly
        rocketMQTemplate.syncSendOrderly("orderly_topic", MessageBuilder.withPayload("Hello, World").build(), "hashkey");

    /*    try {
            // Build a SpringMessage for sending in transaction
            Message msg = MessageBuilder.withPayload();
            // In sendMessageInTransaction(), the first parameter transaction name ("test")
            // must be same with the @RocketMQTransactionListener's member field 'transName'
            rocketMQTemplate.sendMessageInTransaction("test", "test-topic", msg, null);
        } catch (MQClientException e) {
            e.printStackTrace(System.out);
        }*/

    }

    @Data
    @AllArgsConstructor
    public class OrderPaidEvent {
        private String orderId;

        private BigDecimal paidMoney;
    }

    @RocketMQTransactionListener
    class TransactionListenerImpl implements RocketMQLocalTransactionListener {
        @Override
        public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
            // ... local transaction process, return bollback, commit or unknown
            return RocketMQLocalTransactionState.UNKNOWN;
        }

        @Override
        public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
            // ... check transaction status and return bollback, commit or unknown
            return RocketMQLocalTransactionState.COMMIT;
        }
    }
}
