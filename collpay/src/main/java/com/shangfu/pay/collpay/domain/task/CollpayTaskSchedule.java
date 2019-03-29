package com.shangfu.pay.collpay.domain.task;

import com.shangfu.pay.collpay.domain.dao.CollpayInfoRespository;
import com.shangfu.pay.collpay.domain.entity.CollpayInfo;
import com.shangfu.pay.collpay.domain.service.NoticeService;
import com.shangfu.pay.collpay.domain.service.QueryService;
import com.shangfu.pay.collpay.domain.service.impl.NoticeServiceImpl;
import com.shangfu.pay.collpay.domain.service.impl.QueryServiceImpl;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

/**
 * Created by tinlly to 2019/3/14
 * Package for com.shangfu.pay.collpay.domain.task
 */
@Component
@Log
public class CollpayTaskSchedule {

    /**
     * 代付代定时任务类
     */

    @Autowired
    CollpayInfoRespository collpayInfoRespository;

    @Autowired
    QueryServiceImpl queryCollpay;

    @Autowired
    NoticeService noticeService;

    @Autowired
    ExecutorService executorService;

    /**
     * 查询任务
     * 获取所有的订单
     * 对正在处理中的订单状态进行更新
     */
    @Scheduled(fixedDelay = 20000)
//    @Scheduled(cron = "*/10 * * * * ?")
    @Async
    public void pollingTask() {
        List<CollpayInfo> all = this.collpayInfoRespository.findAll();
        for (CollpayInfo collPayOrder : all) {
            // 订单状态为处理中的才进行查询
            // 当订单发送成功时
            if ("SUCCESS".equals(collPayOrder.getStatus())) {
                // 将处理中的订单进行更新
                if ("PROCESSING".equals(collPayOrder.getTrade_state())) {
                    // 返回更新结果
                    Future<Integer> submit = executorService.submit(() -> queryCollpay.queryToUp(collPayOrder));
                    try {
                        Integer executorResult = submit.get(10000, TimeUnit.MILLISECONDS);
                        this.log.info(0 != executorResult ? "updated --> 查询并更新\n" : "none updated --> 查询未更新\n");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        this.log.info("error --> 查询超时\n");
                        e.printStackTrace();
                    }
                }
            }
        }
//        System.out.println("我在轮寻");
    }

    /**
     * 通知任务
     * 获取所有的订单
     * 判断 notice_status 状态
     * 将完成的订单通知到下游
     */
    @Scheduled(fixedDelay = 30000)
    @Async
    public void noticeTask() {
        // 获取所有的订单
        List<CollpayInfo> all = collpayInfoRespository.findAll();
        for (CollpayInfo collPayOrder : all) {
            // 将交易成功且未通知的订单通知给下游
            if ("SUCCESS".equals(collPayOrder.getTrade_state()) && !("true".equals(collPayOrder.getNotice_status()))) {
                executorService.submit(() -> {
                    try {
                        noticeService.noticeForDown(collPayOrder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }


}
