package com.hb.domain.order.service;

import com.hb.domain.order.adapter.port.IAliPayPort;
import com.hb.domain.order.adapter.port.IProductPort;
import com.hb.domain.order.adapter.repository.IOrderRepository;
import com.hb.domain.order.model.aggregate.CreateOrderAggregate;
import com.hb.domain.order.model.entity.MarketPayDiscountEntity;
import com.hb.domain.order.model.entity.PayOrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class OrderService extends AbstractOrderService{

    public OrderService(IOrderRepository repository, IProductPort productPort, IAliPayPort aliPayPort) {
        super(repository, productPort, aliPayPort);
    }

    @Override
    protected MarketPayDiscountEntity lockMarketPayOrder(String userId, String teamId, Long activityId, String productId, String orderId) {
        return productPort.lockMarketPayOrder(userId,teamId,activityId,productId,orderId);
    }

    @Override
    protected void doSaveOrder(CreateOrderAggregate orderAggregate) {
        repository.doSaveOrder(orderAggregate);
    }


    @Override
    public void changeOrderPaySuccess(String orderId) {
        repository.changeOrderPaySuccess(orderId);
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return repository.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return repository.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return repository.changeOrderClose(orderId);
    }
}
