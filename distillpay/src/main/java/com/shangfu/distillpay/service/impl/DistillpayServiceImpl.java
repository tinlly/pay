package com.shangfu.distillpay.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfu.distillpay.dao.DistillpayInfoRepository;
import com.shangfu.distillpay.dao.DownSpInfoRespository;
import com.shangfu.distillpay.entity.DistillpayInfo;
import com.shangfu.distillpay.entity.DownSpInfo;
import com.shangfu.distillpay.mq.CollpayProducer;
import com.shangfu.distillpay.service.DistillpayService;
import com.shangfu.distillpay.util.AesUtils;
import com.shangfu.distillpay.util.RSAUtils;
import com.shangfu.distillpay.util.SignUtils;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;

/**
 * 交易接口
 */
@Service
@Log
public class DistillpayServiceImpl implements DistillpayService {

    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    @Autowired
    DistillpayInfoRepository distillpayInfoRepository;

    @Autowired
    CollpayProducer collpayProducer;

    String methodUrl = "http://testapi.shangfudata.com/gate/rtp/distillpay";
    String signKey = "36D2F03FA9C94DCD9ADE335AC173CCC3";
    String aesKey = "45FBC053B1913EE83BE7C2801B263F3F";//上游给的密钥

    /**
     * 交易方法
     * 1.下游传递一个downCollpayInfo,获取其中的下游机构号
     * 2.调用查询方法，获取当前商户的密钥
     * 3.进行字段解密，获取明文
     */

    public String distillpayToDown(Map<String,String> map) throws Exception {
        Gson gson = new Gson();
        // 从请求参数中删除并且获取 sign
        String sign = map.remove("sign");
        String s = gson.toJson(map);

        DistillpayInfo distillpayInfo = gson.fromJson(s, DistillpayInfo.class);
        String down_sp_id = distillpayInfo.getDown_sp_id();

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);

        //平台的私钥
        String my_pri_key = downSpInfo.get().getMy_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(my_pri_key);
        //商户的公钥
        String down_pub_key = downSpInfo.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        //公钥验签通过后解密
        if (RSAUtils.doCheck(s, sign, rsaPublicKey)){
        distillpayInfo.setCard_name(RSAUtils.privateKeyDecrypt(distillpayInfo.getCard_name(),rsaPrivateKey));
        distillpayInfo.setCard_no(RSAUtils.privateKeyDecrypt(distillpayInfo.getCard_no(), rsaPrivateKey));
        distillpayInfo.setId_no(RSAUtils.privateKeyDecrypt(distillpayInfo.getId_no(), rsaPrivateKey));

            System.out.println("下游请求数据为 > " + ObjectUtil.toString(distillpayInfo));

            distillpayInfoRepository.save(distillpayInfo);

            String p1 = gson.toJson(distillpayInfo);
            collpayProducer.sendMessage("distillpayinfo",p1);

        }
        return "交易失败";

    }

/**
监听队列
 */
    @JmsListener(destination = "distillpayinfo")
    public void distillpayToUp(String distillpay){
//        System.out.println("消费者："+distillpay);

        Gson gson = new Gson();
        DistillpayInfo distillpayInfo = gson.fromJson(distillpay, DistillpayInfo.class);
        //设置上游服务商号及机构号
        distillpayInfo.setSp_id("1000");
        distillpayInfo.setMch_id("100050000000363");

        String distillpayInfotoJson = gson.toJson(distillpayInfo);
        Map distillpayInfotoMap = gson.fromJson(distillpayInfotoJson, Map.class);

        distillpayInfotoMap.remove("down_sp_id");
        distillpayInfotoMap.remove("down_mch_id");
        distillpayInfotoMap.remove("sign");

        distillpayInfotoMap.put("sign", SignUtils.sign(distillpayInfotoMap,signKey));
        System.out.println("看下sign一致不："+distillpayInfotoMap.get("sign"));

        distillpayInfotoMap.replace("card_name", AesUtils.aesEn((String) distillpayInfotoMap.get("card_name"),aesKey));
        distillpayInfotoMap.replace("card_no", AesUtils.aesEn((String) distillpayInfotoMap.get("card_no"),aesKey));
        distillpayInfotoMap.replace("id_no", AesUtils.aesEn((String) distillpayInfotoMap.get("id_no"),aesKey));

        //发送请求
        String responseInfo = HttpUtil.post(methodUrl, distillpayInfotoMap, 6000);
        System.out.println("上游响应："+responseInfo);
        //新对象
        DistillpayInfo distillpayInfo1 = gson.fromJson(responseInfo, DistillpayInfo.class);

        distillpayInfo.setStatus(distillpayInfo1.getStatus());
        distillpayInfo.setCode(distillpayInfo1.getCode());
        distillpayInfo.setMessage(distillpayInfo1.getMessage());
        distillpayInfo.setCh_trade_no(distillpayInfo1.getCh_trade_no());
        distillpayInfo.setTrade_state(distillpayInfo1.getTrade_state());
        distillpayInfo.setErr_code(distillpayInfo1.getErr_code());
        distillpayInfo.setErr_msg(distillpayInfo1.getErr_msg());
        distillpayInfo.setNonce_str(distillpayInfo1.getNonce_str());
        //distillpayInfo.setSign(distillpayInfo1.getSign());

        // 向上游请求成功时 , 存数据库
        if("SUCCESS".equals(distillpayInfo1.getStatus())){
            distillpayInfoRepository.save(distillpayInfo);
        }

        // 上游处理订单状态为成功或失败时 , 通知给下游
        if (!("PROCESSING".equals(distillpayInfo1.getTrade_state()))){
            System.out.println("请求没成功");
        }



    }


}
