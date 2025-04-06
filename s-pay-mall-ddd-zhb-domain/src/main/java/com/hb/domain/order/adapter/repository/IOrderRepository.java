package com.hb.domain.order.adapter.repository;

import com.hb.domain.order.model.aggregate.CreateOrderAggregate;
import com.hb.domain.order.model.entity.OrderEntity;
import com.hb.domain.order.model.entity.PayOrderEntity;
import com.hb.domain.order.model.entity.ShopCartEntity;

import java.util.List;

public interface IOrderRepository {

    OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity);

    void doSaveOrder(CreateOrderAggregate orderAggregate);

    public void updateOrderPayInfo(PayOrderEntity payOrderEntity);

    void changeOrderPaySuccess(String orderId);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);
}
