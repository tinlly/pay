package com.shangfu.pay.epay.domain.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfu.pay.epay.domain.dao.DownSpInfoRespository;
import com.shangfu.pay.epay.domain.dao.EasypayInfoRespository;
import com.shangfu.pay.epay.domain.entity.DownSpInfo;
import com.shangfu.pay.epay.domain.entity.EasypayInfo;
import com.shangfu.pay.epay.domain.service.EasypayService;
import com.shangfu.pay.epay.domain.util.AesUtils;
import com.shangfu.pay.epay.domain.util.RSAUtils;
import com.shangfu.pay.epay.domain.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;


@Service
public class EasypayServiceImpl implements EasypayService {

    @Autowired
    EasypayInfoRespository easypayInfoRespository;
    @Autowired
    DownSpInfoRespository downSpInfoRespository;

//    String methodUrl = "http://192.168.88.65:8888/gate/epay/epapply";
    String methodUrl = "http://testapi.shangfudata.com/gate/epay/epapply";
    String signKey = "00000000000000000000000000000000";
    String aesKey = "77A231F976FF932024B68469EA9823F3";//上游给的密钥

    @Override
    public String downEasypay(String easypayInfoToJson) throws Exception {
        System.out.println("下单请求信息----："+easypayInfoToJson);

        Gson gson = new Gson();

        Map map = gson.fromJson(easypayInfoToJson, Map.class);
        String sign = (String)map.remove("sign");
        String s = gson.toJson(map);

        //下游传递上来的机构id，签名信息
        EasypayInfo easypayInfo = gson.fromJson(easypayInfoToJson, EasypayInfo.class);
        String down_sp_id = easypayInfo.getDown_sp_id();

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);
        //拿到密钥(私钥)
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);
        //拿到密钥(公钥)
        String down_pub_key = downSpInfo.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        //公钥验签
        boolean b = RSAUtils.doCheck(s, sign, rsaPublicKey);
        if (true == b){
            //私钥解密字段
            easypayInfo.setCard_name(RSAUtils.privateKeyDecrypt(easypayInfo.getCard_name(), rsaPrivateKey));
            easypayInfo.setCard_no(RSAUtils.privateKeyDecrypt(easypayInfo.getCard_no(), rsaPrivateKey));
            easypayInfo.setId_no(RSAUtils.privateKeyDecrypt(easypayInfo.getId_no(), rsaPrivateKey));
            easypayInfo.setBank_mobile(RSAUtils.privateKeyDecrypt(easypayInfo.getBank_mobile(), rsaPrivateKey));
            easypayInfo.setCvv2(RSAUtils.privateKeyDecrypt(easypayInfo.getCvv2(), rsaPrivateKey));
            easypayInfo.setCard_valid_date(RSAUtils.privateKeyDecrypt(easypayInfo.getCard_valid_date(), rsaPrivateKey));

            String easypayInfotoJson = gson.toJson(easypayInfo);

            //调用上游交易方法
            return easypayToUp(easypayInfotoJson);
        }

        return "信息错误，交易失败";
    }

    @Override
    public String easypayToUp(String easypayInfoToJson) {
        Gson gson = new Gson();

        Map easypayInfoToMap = gson.fromJson(easypayInfoToJson, Map.class);

        //设置上游服务商号及机构号
        easypayInfoToMap.put("sp_id","1000");
        easypayInfoToMap.put("mch_id","100001000000000001");

        //将json串转为对象，便于存储数据库
        String s = gson.toJson(easypayInfoToMap);
        EasypayInfo easypayInfo = gson.fromJson(s, EasypayInfo.class);
        //移除下游信息
        easypayInfoToMap.remove("down_sp_id");
        easypayInfoToMap.remove("down_mch_id");
        easypayInfoToMap.remove("sign");

        //对上交易信息进行签名
        easypayInfoToMap.put("sign", SignUtils.sign(easypayInfoToMap, signKey));
        //AES加密操作
        easypayInfoToMap.replace("card_name", AesUtils.aesEn((String)easypayInfoToMap.get("card_name"), aesKey));
        easypayInfoToMap.replace("card_no", AesUtils.aesEn((String)easypayInfoToMap.get("card_no"), aesKey));
        easypayInfoToMap.replace("id_no", AesUtils.aesEn((String)easypayInfoToMap.get("id_no"), aesKey));
        easypayInfoToMap.replace("cvv2", AesUtils.aesEn((String)easypayInfoToMap.get("cvv2"), aesKey));
        easypayInfoToMap.replace("card_valid_date", AesUtils.aesEn((String)easypayInfoToMap.get("card_valid_date"), aesKey));
        easypayInfoToMap.replace("bank_mobile", AesUtils.aesEn((String)easypayInfoToMap.get("bank_mobile"), aesKey));
        //发送请求
        String responseInfo = HttpUtil.post(methodUrl, easypayInfoToMap, 12000);
        //获取响应信息，并用一个新CollpayInfo对象装下这些响应信息
        EasypayInfo response = gson.fromJson(responseInfo, EasypayInfo.class);
        //将响应信息存储到当前downCollpayInfo及UpCollpayInfo请求交易完整信息中
        easypayInfo.setTrade_state(response.getTrade_state());
        easypayInfo.setStatus(response.getStatus());
        easypayInfo.setCode(response.getCode());
        easypayInfo.setMessage(response.getMessage());
        easypayInfo.setCh_trade_no(response.getCh_trade_no());
        easypayInfo.setErr_code(response.getErr_code());
        easypayInfo.setErr_msg(response.getErr_msg());

        if("SUCCESS".equals(response.getStatus())){
            //将订单信息表存储数据库
            easypayInfoRespository.save(easypayInfo);
        }else{

        }
        return "响应信息："+responseInfo;
    }
}

