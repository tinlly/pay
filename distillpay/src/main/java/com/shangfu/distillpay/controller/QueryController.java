package com.shangfu.distillpay.controller;

import com.shangfu.distillpay.entity.DistillpayInfo;
import com.shangfu.distillpay.service.impl.QueryServiceImpl;
import com.shangfu.distillpay.util.GsonUtils;
import com.google.gson.Gson;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Log
public class QueryController {

    @Autowired
    QueryServiceImpl queryService;

    @PostMapping("/queryInfo")
    public ResponseEntity<DistillpayInfo> test(@RequestParam String outTradeId){
        //封装进对象
        DistillpayInfo distillpayInfo = new DistillpayInfo();
        distillpayInfo.setOut_trade_no(outTradeId);

        DistillpayInfo distillpayInfo1 = queryService.downQuery(distillpayInfo);
        return ResponseEntity.status(HttpStatus.OK).body(distillpayInfo1);

    }

//    @PostMapping("/upqueryInfo")
//    public ResponseEntity sendDistillpay(@RequestBody String map) throws Exception {
//        Gson gson = new Gson();
////      Map map1 = GsonUtils.getGson().fromJson(map, Map.class);
//        DistillpayInfo response = gson.fromJson(map, DistillpayInfo.class);
//        queryService.upQuery(response);
//        return ResponseEntity.status(HttpStatus.OK).body("交易处理中");
//
//
//    }


    @PostMapping("/querybalanceInfo")
    public ResponseEntity<DistillpayInfo> querybalanceInfo(@RequestParam String mchId){
        //封装进对象
        DistillpayInfo distillpayInfo = new DistillpayInfo();
        distillpayInfo.setMch_id(mchId);

        DistillpayInfo distillpayInfo2 = queryService.upbalanceQuery(distillpayInfo);
        return ResponseEntity.status(HttpStatus.OK).body(distillpayInfo2);


    }
}
