package com.shangfu.distillpay;

import com.google.gson.Gson;
import com.shangfu.distillpay.controller.DistillpayController;
import com.shangfu.distillpay.dao.DistillpayInfoRepository;
import com.shangfu.distillpay.dao.DownSpInfoRespository;
import com.shangfu.distillpay.entity.DistillpayInfo;
import com.shangfu.distillpay.entity.DownSpInfo;
import com.shangfu.distillpay.service.DistillpayService;
import com.shangfu.distillpay.service.impl.DistillpayServiceImpl;
import com.shangfu.distillpay.util.GsonUtils;
import com.shangfu.distillpay.util.RSAUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DistillpayApplicationTests {

    @Autowired
    private DistillpayInfoRepository distillpayInfoRepository;

    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    @Autowired
    DistillpayController distillpayController;

    @Autowired
    DistillpayServiceImpl distillpayService;

//    @Autowired
//    QueryController queryController;


    @Test
    public void contextLoads() throws Exception{
        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById("1001");

        //获取平台的公钥
        String my_pub_key = downSpInfo.get().getMy_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(my_pub_key);

        //获取下游(商户)的私钥
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);

        DistillpayInfo distillpayInfo = new DistillpayInfo();
        distillpayInfo.setDown_sp_id("1001");
        distillpayInfo.setDown_mch_id("101");

        distillpayInfo.setOut_trade_no(System.currentTimeMillis() + "");
        distillpayInfo.setBody("描述");
        distillpayInfo.setTotal_fee("500");
        distillpayInfo.setSettle_acc_type("PERSONNEL");
        distillpayInfo.setBank_name("交通银行3");
        distillpayInfo.setBank_no("123456789123");
        distillpayInfo.setCard_name("交通银行九亭支行");
        distillpayInfo.setCard_no("8888888");
        distillpayInfo.setId_type("ID_CARD");
        distillpayInfo.setId_no("530102192005080136");
        distillpayInfo.setNonce_str("123456789");

        //公钥加密
        distillpayInfo.setCard_name(RSAUtils.publicKeyEncrypt(distillpayInfo.getCard_name(), rsaPublicKey));
        distillpayInfo.setCard_no(RSAUtils.publicKeyEncrypt(distillpayInfo.getCard_no(), rsaPublicKey));
        distillpayInfo.setId_no(RSAUtils.publicKeyEncrypt(distillpayInfo.getId_no(), rsaPublicKey));

        Gson gson = new Gson();
        String s = gson.toJson(distillpayInfo);

        //私钥签名
        distillpayInfo.setSign(RSAUtils.sign(s,rsaPrivateKey));
        /*String sign = easypayInfo.getSign();
        System.out.println("签名信息"+sign);*/

        //转为Json串
        String distillpayInfoToJson = gson.toJson(distillpayInfo);
        System.out.println("加密的Json串>>"+distillpayInfoToJson);
        this.distillpayInfoRepository.save(distillpayInfo);

//        String distillpay = distillpayController.sendDistillpay(distillpayInfoToJson);
        Map map1 = GsonUtils.getGson().fromJson(distillpayInfoToJson, Map.class);
        String distillpay = distillpayService.distillpayToDown(map1);
//        System.out.println(distillpay);

    }


//    @Test
//    public void testQuery(){
//        DistillpayInfo distillpayInfo = new DistillpayInfo();
//        distillpayInfo.setOut_trade_no("1553585607228");
//        Gson gson = new Gson();
//        String s = gson.toJson(distillpayInfo);
//        String query = queryController.Query(s);
//        System.out.println(query);
//    }


}

