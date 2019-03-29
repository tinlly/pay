package com.shangfudata.easypay.mq;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by tinlly to 2019/3/15
 * Package for com.shangfu.pay.collpay.domain.dubbo
 */
@Component
public class NoticeProducer {

    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;

    /**
     * 发送消息
     * @param destinationName 通道名
     * @param message 发送的请求消息
     */
    public void sendMessage(String destinationName , String message) {
        ActiveMQQueue activeMQQueue = new ActiveMQQueue(destinationName);
        jmsMessagingTemplate.convertAndSend(activeMQQueue , message);
    }



}
