package com.shangfu.pay.epay.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EpayApplication {

    /**
     * 快捷
     * 银行卡尾号 :
     * 1 等待交易
     * 2 交易失败
     * 3 交易处理中
     */
    public static void main(String[] args) {
        SpringApplication.run(EpayApplication.class, args);
    }

}
