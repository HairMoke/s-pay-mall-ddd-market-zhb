package com.hb.domain.order.service;

import com.hb.domain.order.adapter.port.IAliPayPort;
import com.hb.domain.order.adapter.port.IProductPort;
import com.hb.domain.order.adapter.repository.IOrderRepository;
import com.hb.domain.order.model.aggregate.CreateOrderAggregate;
import com.hb.domain.order.model.entity.MarketPayDiscountEntity;
import com.hb.domain.order.model.entity.OrderEntity;
import com.hb.domain.order.model.entity.PayOrderEntity;
import com.hb.domain.order.model.valobj.MarketTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
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
    public void changeOrderPaySuccess(String orderId, Date payTime) {
        OrderEntity orderEntity = repository.queryOrderByOrderId(orderId);
        if(null == orderEntity)
            return ;
        if(MarketTypeVO.GROUP_BUY_MARKET.getCode().equals(orderEntity.getMarketType())) {
            repository.changeMarketOrderPaySuccess(orderId);
            // 发起营销结算， 这个过程可以是http/rpc直接调用，也可以发一个商城交易支付完成的消息，之后拼团系统自己接收做结算。
            productPort.settlementMarketPayOrder(orderEntity.getUserId(), orderId, payTime);
            // 注意： 在公司中，发起结算的http/rpc调用可能会失败，这个时候还会有增加job任务补偿。条件为，检查一笔走了拼团的订单，超过n分钟后，仍然没有做拼团结算状态变更。
            // 我们这里失败了，会抛异常，借助支付宝回调/job来重试。你可以单独实现一个独立的job来处理。

        } else{
            repository.changeOrderPaySuccess(orderId, payTime);
        }


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

    @Override
    public void changeOrderMarketSettlement(List<String> outTradeNoList) {
        repository.changeOrderMarketSettlement(outTradeNoList);
    }
}
