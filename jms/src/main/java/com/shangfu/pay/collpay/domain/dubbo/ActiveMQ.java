package com.shangfu.pay.collpay.domain.dubbo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by tinlly to 2019/3/22
 * Package for com.shangfu.pay.collpay.domain.dubbo
 */
@Component
public class ActiveMQ {
    static int count = 0;

    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;

    @JmsListener(destination = "down.notice")
    public void test(String message){
        System.out.println(++count + "<>" + "得到响应信息 > " + message);
    }

}
