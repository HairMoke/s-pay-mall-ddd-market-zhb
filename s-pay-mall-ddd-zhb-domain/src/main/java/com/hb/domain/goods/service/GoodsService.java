package com.hb.domain.goods.service;


import com.hb.domain.goods.adapter.repository.IGoodsRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 结算服务
 */
@Service
public class GoodsService implements IGoodsService{

    @Resource
    private IGoodsRepository repository;

    @Override
    public void changeOrderDealDone(String tradeNo) {
        repository.changeOrderDealDone(tradeNo);
    }
}
