package com.shangfudata.collpay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.collpay.dao.CollpayInfoRespository;
import com.shangfudata.collpay.dao.DownSpInfoRespository;
import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.entity.DownSpInfo;
import com.shangfudata.collpay.exception.*;
import com.shangfudata.collpay.jms.CollpaySenderService;
import com.shangfudata.collpay.service.CollpayService;
import com.shangfudata.collpay.util.AesUtils;
import com.shangfudata.collpay.util.DataValidationUtils;
import com.shangfudata.collpay.util.RSAUtils;
import com.shangfudata.collpay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 交易接口
 */

@Service
public class CollpayServiceImpl implements CollpayService {
    Map responseMap = null;

    @Autowired
    CollpayInfoRespository collpayInfoRespository;
    @Autowired
    DownSpInfoRespository downSpInfoRespository;
    @Autowired
    CollpaySenderService collpaySenderService;

    String methodUrl = "http://testapi.shangfudata.com/gate/cp/collpay";
    String signKey = "00000000000000000000000000000000";
    String aesKey = "77A231F976FF932024B68469EA9823F3";

    /**
     * 交易方法
     * 1.下游传递一个json,获取其中的下游机构号以及签名
     * 2.调用查询方法，获取当前商户的密钥
     * 3.进行验签，字段解密，获取明文、
     * 4.调用向上交易请求方法，参数为collpay对象
     */
    public String downCollpay(String CollpayInfoToJson) throws Exception {
        responseMap = new HashMap();
        DataValidationUtils dataValidationUtils = DataValidationUtils.builder();

        Gson gson = new Gson();

        Map map = gson.fromJson(CollpayInfoToJson, Map.class);


        String message = dataValidationUtils.isNullValid(map);

        if (!(message.equals(""))) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", message);
            return gson.toJson(responseMap);
        }

        String sign = (String) map.remove("downDecoding");
        String s = gson.toJson(map);

        //下游传递上来的机构id，签名信息
        CollpayInfo collpayInfo = gson.fromJson(CollpayInfoToJson, CollpayInfo.class);
        String down_sp_id = collpayInfo.getDown_sp_id();

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
            downDecoding(collpayInfo, rsaPrivateKey);

            String collpayInfoToJson = gson.toJson(collpayInfo);

            // 保存下游请求信息到数据库
            collpayInfoRespository.save(collpayInfo);
            // 将信息发送到队列中
            collpaySenderService.sendMessage("collpayinfo.test", collpayInfoToJson);

            // 异常处理
            dataValidationUtils.processCollPayException(collpayInfo , responseMap);

            // 异常处理后判断是否需要返回
            if("FAIL".equals(responseMap.get("status"))){
                return gson.toJson(responseMap);
            }

            // 响应数据
            responseMap.put("sp_id",collpayInfo.getDown_sp_id());
            responseMap.put("mch_id",collpayInfo.getDown_mch_id());
            responseMap.put("status", "SUCCESS");
            responseMap.put("trade_state", "正在处理中");
            return gson.toJson(responseMap);
        }
        responseMap.put("status", "FAIL");
        responseMap.put("message", "签名错误");
        return gson.toJson(responseMap);
    }

    /**
     * 向上交易方法
     *
     * @param collpayInfoToJson
     * @return 1.设置上游机构号和商户号
     * 2.删除下游机构号和商户号以及签名
     * 3.向上签名，加密，发送请求
     * 4.收到响应信息，存入传上来的collpay对象
     * 5.判断，保存数据库
     */
    @JmsListener(destination = "collpayinfo.test")
    public void collpayToUp(String collpayInfoToJson) {
        //responseMap = new HashMap();
        Gson gson = new Gson();

        Map collpayInfoToMap = gson.fromJson(collpayInfoToJson, Map.class);

        //设置上游服务商号及机构号
        collpayInfoToMap.put("sp_id", "1000");
        collpayInfoToMap.put("mch_id", "100001000000000001");

        //将json串转为对象，便于存储数据库
        String s = gson.toJson(collpayInfoToMap);
        CollpayInfo collpayInfo = gson.fromJson(s, CollpayInfo.class);

        //移除下游信息
        collpayInfoToMap.remove("down_sp_id");
        collpayInfoToMap.remove("down_mch_id");
        collpayInfoToMap.remove("downDecoding");
        //对上交易信息进行签名
        collpayInfoToMap.put("sign", SignUtils.sign(collpayInfoToMap, signKey));
        //AES加密操作
        upEncoding(collpayInfoToMap, aesKey);

        //发送请求
        String responseInfo = HttpUtil.post(methodUrl, collpayInfoToMap, 12000);
        //获取响应信息，并用一个新CollpayInfo对象装下这些响应信息
        CollpayInfo response = gson.fromJson(responseInfo, CollpayInfo.class);

        //将响应信息存储到当前downCollpayInfo及UpCollpayInfo请求交易完整信息中
        collpayInfo.setTrade_state(response.getTrade_state());
        collpayInfo.setStatus(response.getStatus());
        collpayInfo.setCode(response.getCode());
        collpayInfo.setMessage(response.getMessage());
        collpayInfo.setCh_trade_no(response.getCh_trade_no());
        collpayInfo.setErr_code(response.getErr_code());
        collpayInfo.setErr_msg(response.getErr_msg());

        if ("SUCCESS".equals(response.getStatus())) {
            //将订单信息表存储数据库
            collpayInfoRespository.save(collpayInfo);
        }

        // 向下通知上游处理订单情况
    }

    /**
     * RSA 解密方法
     *
     * @param collpayInfo
     * @param rsaPrivateKey
     */
    public void downDecoding(CollpayInfo collpayInfo, RSAPrivateKey rsaPrivateKey) throws Exception {
        collpayInfo.setCard_name(RSAUtils.privateKeyDecrypt(collpayInfo.getCard_name(), rsaPrivateKey));
        collpayInfo.setCard_no(RSAUtils.privateKeyDecrypt(collpayInfo.getCard_no(), rsaPrivateKey));
        collpayInfo.setId_no(RSAUtils.privateKeyDecrypt(collpayInfo.getId_no(), rsaPrivateKey));
        collpayInfo.setBank_mobile(RSAUtils.privateKeyDecrypt(collpayInfo.getBank_mobile(), rsaPrivateKey));
        collpayInfo.setCvv2(RSAUtils.privateKeyDecrypt(collpayInfo.getCvv2(), rsaPrivateKey));
        collpayInfo.setCard_valid_date(RSAUtils.privateKeyDecrypt(collpayInfo.getCard_valid_date(), rsaPrivateKey));
    }

    /**
     * AES 加密方法
     *
     * @param map
     * @param aesKey
     */
    public void upEncoding(Map map, String aesKey) {
        map.replace("card_name", AesUtils.aesEn((String) map.get("card_name"), aesKey));
        map.replace("card_no", AesUtils.aesEn((String) map.get("card_no"), aesKey));
        map.replace("id_no", AesUtils.aesEn((String) map.get("id_no"), aesKey));
        map.replace("cvv2", AesUtils.aesEn((String) map.get("cvv2"), aesKey));
        map.replace("card_valid_date", AesUtils.aesEn((String) map.get("card_valid_date"), aesKey));
        map.replace("bank_mobile", AesUtils.aesEn((String) map.get("bank_mobile"), aesKey));
    }

}
