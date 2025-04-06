package com.hb.infrastructure.adapter.port;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.hb.domain.order.adapter.port.IAliPayPort;
import com.hb.domain.order.model.entity.MarketPayDiscountEntity;
import com.hb.domain.order.model.entity.PayOrderEntity;
import com.hb.domain.order.model.valobj.MarketTypeVO;
import com.hb.domain.order.model.valobj.OrderStatusVO;
import com.hb.infrastructure.adapter.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Component
public class AliPayPort implements IAliPayPort {

    @Value("${alipay.notify_url}")
    private String notifyUrl;
    @Value("${alipay.return_url}")
    private String returnUrl;
    @Resource
    private AlipayClient alipayClient;
    @Resource
    private OrderRepository repository;

    @Override
    public PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException {
        return doPrepayOrder(userId,productId,productName,orderId,totalAmount,null);
    }

    @Override
    public PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount, MarketPayDiscountEntity marketPayDiscountEntity) throws AlipayApiException {
        // 支付金额
        BigDecimal payAmount = null == marketPayDiscountEntity ? totalAmount : marketPayDiscountEntity.getPayPrice();

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        bizContent.put("total_amount", payAmount.toString());
        bizContent.put("subject", productName);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();

        PayOrderEntity payOrderEntity = new PayOrderEntity();
        payOrderEntity.setOrderId(orderId);
        payOrderEntity.setPayUrl(form);
        payOrderEntity.setOrderStatus(OrderStatusVO.PAY_WAIT);

        // 营销信息
        payOrderEntity.setMarketType(null == marketPayDiscountEntity ? MarketTypeVO.NO_MARKET.getCode():MarketTypeVO.GROUP_BUY_MARKET.getCode());
        payOrderEntity.setMarketDeductionAmount(null == marketPayDiscountEntity ? BigDecimal.ZERO : marketPayDiscountEntity.getDeductionPrice());
        payOrderEntity.setPayAmount(payAmount);

        repository.updateOrderPayInfo(payOrderEntity);

        return payOrderEntity;
    }
}
