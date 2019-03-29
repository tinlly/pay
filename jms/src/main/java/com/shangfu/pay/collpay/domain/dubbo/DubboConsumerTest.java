package com.shangfu.pay.collpay.domain.dubbo;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Component;

/**
 * Created by tinlly to 2019/3/20
 * Package for com.activemq.jms.dubbo
 */
//@Component
public class DubboConsumerTest {

    /**
     * dubbo 消费者
     */

    //@Reference(version = "1.0")
    //DubboService dubboService;

    // 消费者
    public void test(){
        System.out.println("执行了 test 方法");
        //dubboService.test();
    }

}
