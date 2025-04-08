package com.hb.test;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.EventBus;
import com.hb.domain.order.adapter.event.PaySuccessMessageEvent;
import com.hb.infrastructure.event.EventPublisher;
import com.hb.types.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Resource
    private EventBus eventBus;

    @Resource
    private PaySuccessMessageEvent paySuccessMessageEvent;

    @Resource
    private EventPublisher eventPublisher;

    @Value("${spring.rabbitmq.config.producer.topic_order_pay_success.routing_key}")
    private String TOPIC_ORDER_PAY_SUCCESS;

    @Test
    public void test() throws InterruptedException {
        BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> paySuccessMessageEventMessage = paySuccessMessageEvent.buildEventMessage(
                PaySuccessMessageEvent.PaySuccessMessage.builder()
                        .tradeNo("")
                        .build()
        );

        eventBus.post(JSON.toJSONString(paySuccessMessageEventMessage.getData()));
        log.info("测试完成");
        new CountDownLatch(1).await();
    }

    @Test
    public void test_eventPublisher() throws InterruptedException {
        BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> paySuccessMessageEventMessage = paySuccessMessageEvent.buildEventMessage(
                PaySuccessMessageEvent.PaySuccessMessage.builder()
                        .tradeNo("1100000111")
                        .build()
        );

        eventPublisher.publish(TOPIC_ORDER_PAY_SUCCESS, JSON.toJSONString(paySuccessMessageEventMessage));

        new CountDownLatch(1).await();
    }

}
