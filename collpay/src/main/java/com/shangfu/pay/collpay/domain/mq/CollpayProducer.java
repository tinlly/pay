package com.shangfu.pay.collpay.domain.mq;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.digest.MD5;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;

/**
 * Created by tinlly to 2019/3/15
 * Package for com.shangfu.pay.collpay.domain.dubbo
 */
@Component
public class CollpayProducer {

    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;
    //@Autowired
    //ExecutorService executorService;

    /**
     * 发送消息
     * @param destinationName 通道名
     * @param message 发送的请求消息
     */
    public void sendMessage(String destinationName , String message) {
        /****************************************************************************
        * 要用到多线程吗?
        ****************************************************************************/
        ActiveMQQueue activeMQQueue = new ActiveMQQueue(destinationName);
        jmsMessagingTemplate.convertAndSend(activeMQQueue , message);
    }



}
