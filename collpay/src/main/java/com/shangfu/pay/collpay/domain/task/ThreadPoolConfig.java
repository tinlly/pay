package com.shangfu.pay.collpay.domain.task;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tinlly to 2019/3/15
 * Package for com.shangfu.pay.collpay.domain.task
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService getExecutorPool(){
        return Executors.newWorkStealingPool(20);
    }

}
