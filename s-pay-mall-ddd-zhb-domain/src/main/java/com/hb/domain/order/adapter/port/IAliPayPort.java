package com.hb.domain.order.adapter.port;

import com.alipay.api.AlipayApiException;
import com.hb.domain.order.model.entity.MarketPayDiscountEntity;
import com.hb.domain.order.model.entity.PayOrderEntity;

import java.math.BigDecimal;

public interface IAliPayPort {

    PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException;

    PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount, MarketPayDiscountEntity marketPayDiscountEntity) throws AlipayApiException;

}
