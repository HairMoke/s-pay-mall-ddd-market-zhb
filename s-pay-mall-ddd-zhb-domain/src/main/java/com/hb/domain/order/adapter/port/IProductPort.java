package com.hb.domain.order.adapter.port;

import com.hb.domain.order.model.entity.MarketPayDiscountEntity;
import com.hb.domain.order.model.entity.ProductEntity;

import java.util.Date;

public interface IProductPort {
    ProductEntity queryProductByProductId(String productId);

    MarketPayDiscountEntity lockMarketPayOrder(String userId, String teamId, Long activityId, String productId, String orderId);


    void settlementMarketPayOrder(String userId, String orderId, Date orderTime);
}
