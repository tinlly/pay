package com.shangfu.gatewaypay;

import com.google.gson.Gson;
import com.shangfu.gatewaypay.dao.DownSpInfoRespository;
import com.shangfu.gatewaypay.entity.DownSpInfo;
import com.shangfu.gatewaypay.entity.GatewaypayInfo;
import com.shangfu.gatewaypay.util.RSAUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GatewaypayApplicationTests {

    @Autowired
    DownSpInfoRespository downSpInfoRespository;

//    @Autowired
//    GatewaypayController gatewaypayController;

//    @Autowired
//    QueryController queryController;

    @Test
    public void contextLoads() throws Exception{

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById("1001");

        //获取平台的公钥
        String my_pub_key = downSpInfo.get().getMy_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(my_pub_key);

        //获取下游自己的私钥
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);

        GatewaypayInfo gatewaypayInfo = new GatewaypayInfo();
        gatewaypayInfo.setDown_sp_id("1001");
        gatewaypayInfo.setDown_mch_id("101");
        gatewaypayInfo.setOut_trade_no(System.currentTimeMillis() + "");
        gatewaypayInfo.setTotal_fee("300");
        gatewaypayInfo.setBody("测试用");
//        gatewaypayInfo.setNotify_url("http://192.168.88.188:8104/gatewaypay/notice");
        gatewaypayInfo.setNotify_url("http://192.168.88.239:9003/shangfu/gatewaypay/notice");
        gatewaypayInfo.setCall_back_url("http://192.168.88.188");
        gatewaypayInfo.setCard_type("CREDIT");
        gatewaypayInfo.setBank_code("01030000");
        gatewaypayInfo.setNonce_str("123456789");

        Gson gson = new Gson();
        String s = gson.toJson(gatewaypayInfo);
        //私钥签名
        gatewaypayInfo.setSign(RSAUtils.sign(s,rsaPrivateKey));
        String gatewaypayInfoToJson = gson.toJson(gatewaypayInfo);

        System.out.println("加密后的json串为："+gatewaypayInfoToJson);
        //String gatewaypay = gatewaypayController.Gatewaypay(gatewaypayInfoToJson);
        // System.out.println(gatewaypay);
    }

//    @Test
//    public void testQuery(){
//        GatewaypayInfo gatewaypayInfo = new GatewaypayInfo();
//        gatewaypayInfo.setOut_trade_no("1553671639717");
//        Gson gson = new Gson();
//        String s = gson.toJson(gatewaypayInfo);
//        //System.out.println(s);
//        String query = queryController.Query(s);
//        System.out.println(query);
//    }

}
