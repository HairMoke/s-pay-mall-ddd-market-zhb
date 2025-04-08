package com.hb.infrastructure.adapter.repository;

import com.alibaba.fastjson2.JSON;
import com.google.common.eventbus.EventBus;
import com.hb.domain.order.adapter.event.PaySuccessMessageEvent;
import com.hb.domain.order.adapter.repository.IOrderRepository;
import com.hb.domain.order.model.aggregate.CreateOrderAggregate;
import com.hb.domain.order.model.entity.OrderEntity;
import com.hb.domain.order.model.entity.PayOrderEntity;
import com.hb.domain.order.model.entity.ProductEntity;
import com.hb.domain.order.model.entity.ShopCartEntity;
import com.hb.domain.order.model.valobj.MarketTypeVO;
import com.hb.domain.order.model.valobj.OrderStatusVO;
import com.hb.infrastructure.dao.IOrderDao;
import com.hb.infrastructure.dao.po.PayOrder;
import com.hb.infrastructure.event.EventPublisher;
import com.hb.types.common.Constants;
import com.hb.types.event.BaseEvent;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Repository
public class OrderRepository implements IOrderRepository {

    @Resource
    private IOrderDao orderDao;

    @Resource
    private PaySuccessMessageEvent paySuccessMessageEvent;

    @Resource
    private EventBus eventBus;

    @Resource
    private EventPublisher eventPublisher;

    @Override
    public OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity) {
        // 1. 封装参数
        PayOrder orderReq = new PayOrder();
        orderReq.setUserId(shopCartEntity.getUserId());
        orderReq.setProductId(shopCartEntity.getProductId());

        // 2. 查询到订单
        PayOrder order = orderDao.queryUnPayOrder(orderReq);
        if (null == order) return null;

        // 3. 返回结果
        return OrderEntity.builder()
                .productId(order.getProductId())
                .productName(order.getProductName())
                .orderId(order.getOrderId())
                .orderStatusVO(OrderStatusVO.valueOf(order.getStatus()))
                .orderTime(order.getOrderTime())
                .totalAmount(order.getTotalAmount())
                .payUrl(order.getPayUrl())
                .marketType(order.getMarketType())
                .marketDeductionAmount(order.getMarketDeductionAmount())
                .payAmount(order.getPayAmount())
                .build();
    }

    @Override
    public void doSaveOrder(CreateOrderAggregate orderAggregate) {
        String userId = orderAggregate.getUserId();
        ProductEntity productEntity = orderAggregate.getProductEntity();
        OrderEntity orderEntity = orderAggregate.getOrderEntity();

        PayOrder order = new PayOrder();
        order.setUserId(userId);
        order.setProductId(productEntity.getProductId());
        order.setProductName(productEntity.getProductName());
        order.setOrderId(orderEntity.getOrderId());
        order.setOrderTime(orderEntity.getOrderTime());
        order.setTotalAmount(productEntity.getPrice());
        order.setStatus(orderEntity.getOrderStatusVO().getCode());
        order.setMarketType(MarketTypeVO.NO_MARKET.getCode());
        order.setMarketDeductionAmount(BigDecimal.ZERO);
        order.setPayAmount(productEntity.getPrice());
        order.setMarketType(orderEntity.getMarketType());

        orderDao.insert(order);
    }

    @Override
    public void updateOrderPayInfo(PayOrderEntity payOrderEntity) {
        PayOrder payOrderReq = PayOrder.builder()
                .userId(payOrderEntity.getUserId())
                .orderId(payOrderEntity.getOrderId())
                .status(payOrderEntity.getOrderStatus().getCode())
                .payUrl(payOrderEntity.getPayUrl())
                .marketType(payOrderEntity.getMarketType())
                .marketDeductionAmount(payOrderEntity.getMarketDeductionAmount())
                .payAmount(payOrderEntity.getPayAmount())
                .build();
        orderDao.updateOrderPayInfo(payOrderReq);
    }

    @Override
    public void changeOrderPaySuccess(String orderId, Date payTime) {
        PayOrder payOrderReq = new PayOrder();
        payOrderReq.setOrderId(orderId);
        payOrderReq.setStatus(OrderStatusVO.PAY_SUCCESS.getCode());
        payOrderReq.setPayTime(payTime);
        orderDao.changeOrderPaySuccess(payOrderReq);

        // 不走拼团的直接结算发货
        BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> paySuccessMessageEventMessage = paySuccessMessageEvent.buildEventMessage(
                PaySuccessMessageEvent.PaySuccessMessage.builder()
                .tradeNo(orderId)
                .build());
        PaySuccessMessageEvent.PaySuccessMessage paySuccessMessage = paySuccessMessageEventMessage.getData();

        // 旧版发送消息方式
//        eventBus.post(JSON.toJSONString(paySuccessMessage));
        eventPublisher.publish(paySuccessMessageEvent.topic(), JSON.toJSONString(paySuccessMessage));
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return orderDao.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return orderDao.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return orderDao.changeOrderClose(orderId);
    }

    @Override
    public OrderEntity queryOrderByOrderId(String orderId) {
        PayOrder payOrder = orderDao.queryOrderByOrderId(orderId);
        if(null == payOrder) {
            return null;
        }

        return OrderEntity.builder()
                .userId(payOrder.getUserId())
                .productId(payOrder.getProductId())
                .productName(payOrder.getProductName())
                .orderId(payOrder.getOrderId())
                .orderTime(payOrder.getOrderTime())
                .totalAmount(payOrder.getTotalAmount())
                .payUrl(payOrder.getPayUrl())
                .marketType(payOrder.getMarketType())
                .marketDeductionAmount(payOrder.getMarketDeductionAmount())
                .payAmount(payOrder.getPayAmount())
                .build();
    }

    @Override
    public void changeMarketOrderPaySuccess(String orderId) {
        PayOrder payOrderReq = new PayOrder();
        payOrderReq.setOrderId(orderId);
        payOrderReq.setStatus(OrderStatusVO.PAY_SUCCESS.getCode());
        orderDao.changeOrderPaySuccess(payOrderReq);
    }

    @Override
    public void changeOrderMarketSettlement(List<String> outTradeNoList) {
        // 更新拼团结算状态
        orderDao.changeOrderMarketSettlement(outTradeNoList);

        // 循环成功发送消息 —— 一般在公司的场景里，还会有job任务扫描超时没有结算的订单，查询订单状态。查询对方服务端的接口，会被限制一次查询多少，频次多少。
        outTradeNoList.forEach(outTradeNo -> {
            BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> paySuccessMessageEventMessage = paySuccessMessageEvent.buildEventMessage(
                    PaySuccessMessageEvent.PaySuccessMessage.builder()
                            .tradeNo(outTradeNo)
                            .build());

            PaySuccessMessageEvent.PaySuccessMessage paySuccessMessage = paySuccessMessageEventMessage.getData();

            // 旧版发送消息方式
//            eventBus.post(JSON.toJSONString(paySuccessMessage));

            eventPublisher.publish(paySuccessMessageEvent.topic(), JSON.toJSONString(paySuccessMessage));
        });
    }
}
