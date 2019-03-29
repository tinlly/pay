package com.shangfu.pay.collpay.domain.controller;

import com.shangfu.pay.collpay.domain.entity.CollpayInfo;
import com.shangfu.pay.collpay.domain.service.impl.QueryServiceImpl;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by tinlly to 2019/3/21
 * Package for com.shangfudata.collpay.controller
 */
@RestController
@Log
public class QueryController {

    /**
     * 查询代收交易
     */
    @Autowired
    QueryServiceImpl queryService;

    @PostMapping("/queryInfo")
    public ResponseEntity<CollpayInfo> test(@RequestParam String outTradeId){
        // 创建对象 , 将 outTradeId 封装进对象中
        CollpayInfo collpayInfo = new CollpayInfo();
        collpayInfo.setOut_trade_no(outTradeId);
//        System.out.println(">>" + outTradeId);
        // 调用查询方法
        CollpayInfo collpayInfo1 = queryService.downQuery(collpayInfo);
        return ResponseEntity.status(HttpStatus.OK).body(collpayInfo1);
    }

}
