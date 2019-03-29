package com.shangfudata.easypay.mq;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

/**
 * Created by tinlly to 2019/3/3
 * Package for com.example.demo.activemq
 */
@Configuration
public class ContainerFactory {

    @Bean
    public JmsListenerContainerFactory simpleJmsListenerContainer(ConnectionFactory connectionFactory){
        SimpleJmsListenerContainerFactory simpleJmsListenerContainerFactory = new SimpleJmsListenerContainerFactory();
        simpleJmsListenerContainerFactory.setConnectionFactory(connectionFactory);
        simpleJmsListenerContainerFactory.setPubSubDomain(false);
        return simpleJmsListenerContainerFactory;
    }

}
