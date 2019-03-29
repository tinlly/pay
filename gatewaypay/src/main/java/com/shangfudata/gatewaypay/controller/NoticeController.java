package com.shangfudata.gatewaypay.controller;

import com.shangfudata.gatewaypay.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/gatewaypay")
public class NoticeController {

    @Autowired
    NoticeService noticeService;


    @PostMapping("/notice")
    @ResponseBody
    public ResponseEntity<String> noticeUpTest(@RequestParam Map<String , String> map) {
        System.out.println("获取上游通知的内容 > " + map);
        String s = noticeService.Upnotice(map);
        return ResponseEntity.status(HttpStatus.OK).body(s);
    }

    //@ResponseBody
    //public String notice(@RequestParam("out_trade_no") String out_trade_no,
    //                     @RequestParam("trade_state") String trade_state){
    //    //传入订单号以及交易状态
    //    return noticeService.Upnotice(out_trade_no,trade_state);
    //}
}
