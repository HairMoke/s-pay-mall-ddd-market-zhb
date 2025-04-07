package com.hb.domain.order.service;

import com.alipay.api.AlipayApiException;
import com.hb.domain.order.model.entity.PayOrderEntity;
import com.hb.domain.order.model.entity.ShopCartEntity;

import java.util.Date;
import java.util.List;

public interface IOrderService {

    PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws AlipayApiException;

    void changeOrderPaySuccess(String orderId, Date orderTime);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);

    void changeOrderMarketSettlement(List<String> outTradeNoList);

}
