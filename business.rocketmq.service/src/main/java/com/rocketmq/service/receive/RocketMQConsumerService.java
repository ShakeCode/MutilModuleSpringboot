package com.rocketmq.service.receive;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(
        topic = "rocketmq.topic",
        consumerGroup = "rocketmq.consumer.group"
)
public class RocketMQConsumerService implements RocketMQListener<String> {

    @Override
    public void onMessage(String s) {
        System.out.printf("------- StringConsumer received: %s %f", s);
    }
}
