package com.hb.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.google.common.eventbus.Subscribe;
import com.hb.domain.goods.service.IGoodsService;
import com.hb.domain.order.adapter.event.PaySuccessMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 支付结算成功回调消息
 */


@Slf4j
@Component
public class OrderPaySuccessListener {

    @Resource
    private IGoodsService goodsService;

    // 旧版发布订阅方式
//    @Subscribe
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.consumer.topic_order_pay_success.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.consumer.topic_order_pay_success.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.consumer.topic_order_pay_success.routing_key}"
            )
    )
    public void handleEvent(String paySuccessMessageJson) {
        try {
            log.info("收到支付成功消息，可以做接下来的事情，如；发货、充值、开户员、返利 {}", paySuccessMessageJson);

            PaySuccessMessageEvent.PaySuccessMessage paySuccessMessage = JSON.parseObject(paySuccessMessageJson, PaySuccessMessageEvent.PaySuccessMessage.class);
            log.info("模拟发货（如；发货、充值、开户员、返利），单号:{}", paySuccessMessage.getTradeNo());

            // 变更订单状态
            goodsService.changeOrderDealDone(paySuccessMessage.getTradeNo());

            // 可以打开测试，MQ 消费失败，会抛异常，之后重试消费。这个也是最终执行的重要手段。
            // throw new RuntimeException("重试消费");
        } catch (Exception e) {
            log.error("收到支付成功消息失败 {}", paySuccessMessageJson,e);
            throw e;
        }







    }
}
