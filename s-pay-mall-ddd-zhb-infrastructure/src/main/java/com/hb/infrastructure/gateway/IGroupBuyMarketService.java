package com.hb.infrastructure.gateway;

import com.hb.infrastructure.gateway.dto.LockMarketPayOrderRequestDTO;
import com.hb.infrastructure.gateway.dto.LockMarketPayOrderResponseDTO;
import com.hb.infrastructure.gateway.dto.SettlementMarketPayOrderRequestDTO;
import com.hb.infrastructure.gateway.dto.SettlementMarketPayOrderResponseDTO;
import com.hb.infrastructure.gateway.response.Response;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 拼团营销
 */

public interface IGroupBuyMarketService {


    /**
     * 营销锁单
     *
     * @param requestDTO 锁单商品信息
     * @return 锁单结果信息
     */
    @POST("api/v1/gbm/trade/lock_market_pay_order")
    Call<Response<LockMarketPayOrderResponseDTO>> lockMarketPayOrder(@Body LockMarketPayOrderRequestDTO requestDTO);

    /**
     * 营销结算
     *
     * @param requestDTO 结算商品信息
     * @return 结算结果信息
     */
    @POST("api/v1/gbm/trade/settlement_market_pay_order")
    Call<Response<SettlementMarketPayOrderResponseDTO>> settlementMarketPayOrder(@Body SettlementMarketPayOrderRequestDTO requestDTO);
}
