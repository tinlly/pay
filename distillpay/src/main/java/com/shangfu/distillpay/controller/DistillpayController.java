package com.shangfu.distillpay.controller;

import com.shangfu.distillpay.service.impl.DistillpayServiceImpl;
import com.shangfu.distillpay.util.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DistillpayController {

    @Autowired
    DistillpayServiceImpl distillpayService;


    /**
     * 下游请求
     */

    @PostMapping("/sendInfo")
    public ResponseEntity sendDistillpay(@RequestBody String map) throws Exception {
        Map map1 = GsonUtils.getGson().fromJson(map, Map.class);

        String s = distillpayService.distillpayToDown(map1);
        return ResponseEntity.status(HttpStatus.OK).body("交易处理中");


    }
}
