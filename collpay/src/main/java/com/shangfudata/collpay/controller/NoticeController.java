package com.shangfudata.collpay.controller;

import com.shangfudata.collpay.dao.CollpayInfoRespository;
import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoticeController {

    @Autowired
    NoticeService noticeService;



    public String notice(String out_trade_no) throws Exception{
        return noticeService.notice(out_trade_no);
    }

}
