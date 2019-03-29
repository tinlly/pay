package com.shangfu.pay.collpay.domain.controller;

import com.shangfu.pay.collpay.domain.service.impl.CollpayServiceImpl;
import com.shangfu.pay.collpay.domain.util.GsonUtils;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Created by tinlly to 2019/3/14
 * Package for com.shangfu.pay.collpay.domain.controller
 */
@RestController
@Log
public class CollpayController {

    @Autowired
    CollpayServiceImpl collpayService;

    /**
     * 下游请求方法
     * @param map 下游传递参数
     * @return 响应结果 : status 响应码 响应体
     * @throws Exception
     */
    @PostMapping("/sendInfo")
    @ResponseBody
    public ResponseEntity sendCollpay(@RequestBody String map) throws Exception {
        //Map<String , String> map

        Map map1 = GsonUtils.getGson().fromJson(map, Map.class);

        // 调用下游处理方法
        String s = collpayService.collpayToDown(map1);
        System.out.println("Post test...");
        return ResponseEntity.status(cn.hutool.http.HttpStatus.HTTP_OK).body("交易处理中");
    }

}