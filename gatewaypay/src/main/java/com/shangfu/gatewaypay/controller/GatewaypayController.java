package com.shangfu.gatewaypay.controller;

import com.shangfu.gatewaypay.service.GatewaypayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gatewaypay")
public class GatewaypayController {

    @Autowired
    GatewaypayService gatewaypayService;


    /**
     * 对下开放请求内部处理接口
     * @param gatewaypayInfoToJson
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/trading", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Gatewaypay(@RequestBody String gatewaypayInfoToJson) throws Exception{

        System.out.println("下游传过来加密后的json串为："+gatewaypayInfoToJson);

        return gatewaypayService.downGatewaypay(gatewaypayInfoToJson);

    }


}
