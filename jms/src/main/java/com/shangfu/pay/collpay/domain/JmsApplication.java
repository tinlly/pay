package com.shangfu.pay.collpay.domain;

import com.shangfu.pay.collpay.domain.dubbo.DubboConsumerTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

@SpringBootApplication
@Controller
public class JmsApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(JmsApplication.class, args);
    }

}