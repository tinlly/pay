package com.shangfu.distillpay.dao;

import com.shangfu.distillpay.DistillpayApplication;
import com.shangfu.distillpay.entity.DistillpayInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DistillpayApplication.class)
public class DistillpayInfoRepositoryTest {

    @Autowired
    private DistillpayInfoRepository distillpayInfoRepository;

    @Test
    public void testSave(){
        DistillpayInfo distillpayInfo = new DistillpayInfo();
        distillpayInfo.setDown_sp_id("1001");
        distillpayInfo.setDown_mch_id("101");
        distillpayInfo.setOut_trade_no(System.currentTimeMillis() + "");
        distillpayInfo.setBody("描述");
        distillpayInfo.setTotal_fee("10000");
        distillpayInfo.setSettle_acc_type("PERSONNEL");
        distillpayInfo.setBank_name("交通银行");
        distillpayInfo.setBank_no("123456789123");
        distillpayInfo.setCard_name("交通银行九亭支行");
        distillpayInfo.setCard_no("8888");
        distillpayInfo.setId_type("身份证");
        distillpayInfo.setId_no("320681111111");
        distillpayInfo.setNonce_str("123456789");
        this.distillpayInfoRepository.save(distillpayInfo);


    }

}