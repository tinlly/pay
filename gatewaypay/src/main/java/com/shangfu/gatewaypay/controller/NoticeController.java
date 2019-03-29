package com.shangfu.gatewaypay.controller;

import com.shangfu.gatewaypay.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gatewaypay")
public class NoticeController {

    @Autowired

    NoticeService noticeService;


    @PostMapping("/notice")
    @ResponseBody
    public String notice(@RequestParam("out_trade_no") String out_trade_no,
                         @RequestParam("trade_state") String trade_state){
       System.out.println("***订单号**"+out_trade_no);
       System.out.println("***订单交易状态**"+trade_state);

        //传入订单号以及交易状态
        return noticeService.Upnotice(out_trade_no,trade_state);
    }
}
