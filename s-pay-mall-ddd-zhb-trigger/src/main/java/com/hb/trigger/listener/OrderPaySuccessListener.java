package com.hb.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.google.common.eventbus.Subscribe;
import com.hb.domain.goods.service.IGoodsService;
import com.hb.domain.order.adapter.event.PaySuccessMessageEvent;
import lombok.extern.slf4j.Slf4j;
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

    @Subscribe
    public void handleEvent(String paySuccessMessageJson) {
        log.info("收到支付成功消息，可以做接下来的事情，如；发货、充值、开户员、返利 {}", paySuccessMessageJson);

        PaySuccessMessageEvent.PaySuccessMessage paySuccessMessage = JSON.parseObject(paySuccessMessageJson, PaySuccessMessageEvent.PaySuccessMessage.class);

        log.info("模拟发货（如；发货、充值、开户员、返利），单号:{}", paySuccessMessage.getTradeNo());

        // 变更订单状态
        goodsService.changeOrderDealDone(paySuccessMessage.getTradeNo());
    }
}
