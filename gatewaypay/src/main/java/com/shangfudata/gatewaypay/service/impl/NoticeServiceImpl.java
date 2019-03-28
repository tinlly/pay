package com.shangfudata.gatewaypay.service.impl;

import com.google.gson.Gson;

import com.shangfudata.gatewaypay.dao.GatewaypayInfoRespository;
import com.shangfudata.gatewaypay.entity.GatewaypayInfo;
import com.shangfudata.gatewaypay.service.NoticeService;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NoticeServiceImpl implements NoticeService {
    
    @Autowired
    GatewaypayInfoRespository gatewaypayInfoRespository;

   /* @Autowired
    EasypaySenderService easypaySenderService;*/


    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;
    
    @Override
    public String Upnotice(String outTradeNo,String tradeState) {

        Gson gson = new Gson();
        /*Map noticeInfoToMap = gson.fromJson(noticeInfoToJson, Map.class);

        String out_trade_no = (String)noticeInfoToMap.get("out_trade_no");*/
        Optional<GatewaypayInfo> gatewaypayInfo = gatewaypayInfoRespository.findById(outTradeNo);

        if(null == gatewaypayInfoRespository.findNoticeStatus(outTradeNo)){
            String easypayInfoToJson = gson.toJson(gatewaypayInfo);
            ToDown("gatewaypaytodown.notice",easypayInfoToJson);
            String noticeStatus = "true";
            gatewaypayInfoRespository.updateNoticeStatus(noticeStatus,outTradeNo);
        }


        return "SUCCESS";
    }

    @Override
    public void ToDown(String destinationName,String NoticeInfoToJson) {
        ActiveMQQueue activeMQQueue = new ActiveMQQueue(destinationName);
        jmsMessagingTemplate.convertAndSend(activeMQQueue , NoticeInfoToJson);
    }


}
