package com.hb.infrastructure.adapter.repository;

import com.hb.domain.goods.adapter.repository.IGoodsRepository;
import com.hb.infrastructure.dao.IOrderDao;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;


/**
 * 结算仓储服务
 */
@Repository
public class GoodsRepository implements IGoodsRepository {

    @Resource
    private IOrderDao orderDao;

    @Override
    public void changeOrderDealDone(String orderId) {
        orderDao.changeOrderDealDone(orderId);
    }
}
