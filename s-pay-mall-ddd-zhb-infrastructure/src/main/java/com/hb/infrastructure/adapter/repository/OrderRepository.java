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
import com.hb.types.common.Constants;
import com.hb.types.event.BaseEvent;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Repository
public class OrderRepository implements IOrderRepository {

    @Resource
    private IOrderDao orderDao;

    @Resource
    private PaySuccessMessageEvent paySuccessMessageEvent;

    @Resource
    private EventBus eventBus;

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
    public void changeOrderPaySuccess(String orderId) {
        PayOrder payOrderReq = new PayOrder();
        payOrderReq.setOrderId(orderId);
        payOrderReq.setStatus(OrderStatusVO.PAY_SUCCESS.getCode());
        orderDao.changeOrderPaySuccess(payOrderReq);

        BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> paySuccessMessageEventMessage = paySuccessMessageEvent.buildEventMessage(PaySuccessMessageEvent.PaySuccessMessage.builder()
                .tradeNo(orderId)
                .build());
        PaySuccessMessageEvent.PaySuccessMessage paySuccessMessage = paySuccessMessageEventMessage.getData();

        eventBus.post(paySuccessMessage);
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
}
