package com.shangfudata.easypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.easypay.dao.DownSpInfoRespository;
import com.shangfudata.easypay.dao.EasypayInfoRespository;
import com.shangfudata.easypay.entity.DownSpInfo;
import com.shangfudata.easypay.entity.EasypayInfo;
import com.shangfudata.easypay.service.EasypayService;
import com.shangfudata.easypay.util.AesUtils;
import com.shangfudata.easypay.util.DataValidationUtils;
import com.shangfudata.easypay.util.RSAUtils;
import com.shangfudata.easypay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class EasypayServiceImpl implements EasypayService {

    @Autowired
    EasypayInfoRespository easypayInfoRespository;
    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    String methodUrl = "http://192.168.88.65:8888/gate/epay/epapply";
    String signKey = "00000000000000000000000000000000";
    String aesKey = "77A231F976FF932024B68469EA9823F3";

    /**
     * 对下开放快捷交易方法
     *
     * @param easypayInfoToJson
     * @return
     * @throws Exception
     */
    public String downEasypay(String easypayInfoToJson) throws Exception {
        Map responseMap = new HashMap();
        Gson gson = new Gson();

        DataValidationUtils builder = DataValidationUtils.builder();


        Map map = gson.fromJson(easypayInfoToJson, Map.class);
        String nullValid = builder.isNullValid(map);

        // 请求参数不为空验证
        if (!("".equals(nullValid))) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", nullValid);
            return gson.toJson(nullValid);
        }

        String sign = (String) map.remove("sign");
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
        if (RSAUtils.doCheck(s, sign, rsaPublicKey)) {
            //私钥解密字段
            easypayInfo.setCard_name(RSAUtils.privateKeyDecrypt(easypayInfo.getCard_name(), rsaPrivateKey));
            easypayInfo.setCard_no(RSAUtils.privateKeyDecrypt(easypayInfo.getCard_no(), rsaPrivateKey));
            easypayInfo.setId_no(RSAUtils.privateKeyDecrypt(easypayInfo.getId_no(), rsaPrivateKey));
            easypayInfo.setBank_mobile(RSAUtils.privateKeyDecrypt(easypayInfo.getBank_mobile(), rsaPrivateKey));
            easypayInfo.setCvv2(RSAUtils.privateKeyDecrypt(easypayInfo.getCvv2(), rsaPrivateKey));
            easypayInfo.setCard_valid_date(RSAUtils.privateKeyDecrypt(easypayInfo.getCard_valid_date(), rsaPrivateKey));

            // 数据合法效验
            builder.processEasyPayException(easypayInfo , responseMap);
            if("FAIL".equals(responseMap.get("status"))){
                responseMap.put("sp_id" , easypayInfo.getSp_id());
                responseMap.put("down_id" , easypayInfo.getMch_id());
                return gson.toJson(nullValid);
            }

            //调用上游交易方法
            return easypayToUp(gson.toJson(easypayInfo));
        }

        responseMap.put("status", "FAIL");
        responseMap.put("message", "签名错误");
        return gson.toJson(responseMap);
    }

    /**
     * 通知给人的内容
     *
     * @param easypayInfoToJson
     * @return
     */
    public String easypayToUp(String easypayInfoToJson) {
        Gson gson = new Gson();
        Map easypayInfoToMap = gson.fromJson(easypayInfoToJson, Map.class);

        //设置上游服务商号及机构号
        easypayInfoToMap.put("sp_id", "1000");
        easypayInfoToMap.put("mch_id", "100001000000000001");

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
        easypayInfoToMap.replace("card_name", AesUtils.aesEn((String) easypayInfoToMap.get("card_name"), aesKey));
        easypayInfoToMap.replace("card_no", AesUtils.aesEn((String) easypayInfoToMap.get("card_no"), aesKey));
        easypayInfoToMap.replace("id_no", AesUtils.aesEn((String) easypayInfoToMap.get("id_no"), aesKey));
        easypayInfoToMap.replace("cvv2", AesUtils.aesEn((String) easypayInfoToMap.get("cvv2"), aesKey));
        easypayInfoToMap.replace("card_valid_date", AesUtils.aesEn((String) easypayInfoToMap.get("card_valid_date"), aesKey));
        easypayInfoToMap.replace("bank_mobile", AesUtils.aesEn((String) easypayInfoToMap.get("bank_mobile"), aesKey));
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

        if ("SUCCESS".equals(response.getStatus())) {
            //将订单信息表存储数据库
            easypayInfoRespository.save(easypayInfo);
        }

        // 向下游返回响应信息
        return responseInfo;
    }
}
