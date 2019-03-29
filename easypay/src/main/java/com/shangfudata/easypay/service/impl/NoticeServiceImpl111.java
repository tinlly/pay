package com.shangfudata.easypay.service.impl;

import com.google.gson.Gson;
import com.shangfudata.easypay.dao.EasypayInfoRespository;
import com.shangfudata.easypay.entity.EasypayInfo;
import com.shangfudata.easypay.jms.EasypaySenderService;
import com.shangfudata.easypay.service.NoticeService111;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NoticeServiceImpl111 implements NoticeService111 {
    
    @Autowired
    EasypayInfoRespository easypayInfoRespository;

    @Autowired
    EasypaySenderService easypaySenderService;


    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;
    
    @Override
    public String Upnotice(String outTradeNo,String tradeState) {

        Gson gson = new Gson();

        Optional<EasypayInfo> easypayInfo = easypayInfoRespository.findById(outTradeNo);

        if(null == easypayInfoRespository.findNoticeStatus(outTradeNo)){
            String easypayInfoToJson = gson.toJson(easypayInfo);
            ToDown("easypaytodown.notice",easypayInfoToJson);
            String noticeStatus = "true";
            easypayInfoRespository.updateNoticeStatus(noticeStatus,outTradeNo);
        }


        return "SUCCESS";
    }

    @Override
    public void ToDown(String destinationName,String NoticeInfoToJson) {
        ActiveMQQueue activeMQQueue = new ActiveMQQueue(destinationName);
        jmsMessagingTemplate.convertAndSend(activeMQQueue , NoticeInfoToJson);
    }


}
