package com.hb.trigger.job;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;

import com.hb.domain.order.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 检测未接收到或未正确处理的支付回调通知
 */

@Slf4j
@Component()
public class NoPayNotifyOrderJob {

    @Resource
    private IOrderService orderService;

    @Resource
    private AlipayClient alipayClient;

    // 3 -> 30 避免控制台打印太多信息
    @Scheduled(cron = "0/30 * * * * ?")
    public void exec(){
        try {
            log.info("任务；检测未接收到或未正确处理的支付回调通知");
            List<String> orderIds = orderService.queryNoPayNotifyOrder();
            if(null == orderIds || orderIds.isEmpty()) {
                return;
            }

            for(String orderId : orderIds) {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel bizModel = new AlipayTradeQueryModel();
                bizModel.setOutTradeNo(orderId);
                request.setBizModel(bizModel);

                AlipayTradeQueryResponse alipayTradeQueryResponse = alipayClient.execute(request);
                String code = alipayTradeQueryResponse.getCode();
                // 判断状态码
                if("10000".equals(code)) {
                    orderService.changeOrderPaySuccess(orderId, alipayTradeQueryResponse.getSendPayDate());
                }
            }
        } catch (Exception e){
            log.error("检测未接收到或未正确处理的支付回调通知失败", e);
        }
    }

}
