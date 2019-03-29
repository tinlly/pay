package com.shangfu.pay.collpay.domain.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfu.pay.collpay.domain.dao.CollpayInfoRespository;
import com.shangfu.pay.collpay.domain.dao.DownSpInfoRespository;
import com.shangfu.pay.collpay.domain.entity.CollpayInfo;
import com.shangfu.pay.collpay.domain.entity.DownSpInfo;
import com.shangfu.pay.collpay.domain.mq.CollpayProducer;
import com.shangfu.pay.collpay.domain.service.CollpayService;
import com.shangfu.pay.collpay.domain.service.NoticeService;
import com.shangfu.pay.collpay.domain.util.AesUtils;
import com.shangfu.pay.collpay.domain.util.RSAUtils;
import com.shangfu.pay.collpay.domain.util.SignUtils;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
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
public class CollpayServiceImpl implements CollpayService {

    @Autowired
    CollpayInfoRespository collpayInfoRespository;
    @Autowired
    DownSpInfoRespository downSpInfoRespository;
    @Autowired
    CollpayProducer collpayProducer;
    //JmsMessagingTemplate jmsMessagingTemplate;
    @Autowired
    NoticeService noticeService;


    String methodUrl = "http://testapi.shangfudata.com/gate/cp/collpay";
    String signKey = "00000000000000000000000000000000";
    String aesKey = "77A231F976FF932024B68469EA9823F3";//上游给的密钥

    /**
     * 交易方法
     * 1.下游传递一个downCollpayInfo,获取其中的下游机构号
     * 2.调用查询方法，获取当前商户的密钥
     * 3.进行字段解密，获取明文
     */
    public String collpayToDown(Map<String , String> map) throws Exception{
        Gson gson = new Gson();
        // 从请求参数中删除并且获取 sign
        String sign = map.remove("sign");
        String s = gson.toJson(map);

        //下游传递上来的机构id，签名信息
        CollpayInfo collpayInfo = gson.fromJson(s, CollpayInfo.class);
        String down_sp_id = collpayInfo.getDown_sp_id();

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);
        //拿到密钥(私钥)
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);
        //拿到密钥(公钥)
        String down_pub_key = downSpInfo.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        // 签名验证
        if (RSAUtils.doCheck(s, sign, rsaPublicKey)){
            //私钥解密字段
            collpayInfo.setCard_name(RSAUtils.privateKeyDecrypt(collpayInfo.getCard_name(), rsaPrivateKey));
            collpayInfo.setCard_no(RSAUtils.privateKeyDecrypt(collpayInfo.getCard_no(), rsaPrivateKey));
            collpayInfo.setId_no(RSAUtils.privateKeyDecrypt(collpayInfo.getId_no(), rsaPrivateKey));
            collpayInfo.setBank_mobile(RSAUtils.privateKeyDecrypt(collpayInfo.getBank_mobile(), rsaPrivateKey));
            collpayInfo.setCvv2(RSAUtils.privateKeyDecrypt(collpayInfo.getCvv2(), rsaPrivateKey));
            collpayInfo.setCard_valid_date(RSAUtils.privateKeyDecrypt(collpayInfo.getCard_valid_date(), rsaPrivateKey));

            this.log.info("下游请求数据为 > " + ObjectUtil.toString(collpayInfo));
            // 发送消息到队列
            // 返回 JSON 串
            // 签名 ok 将数据保存到数据库
            CollpayInfo save = collpayInfoRespository.save(collpayInfo);
            String s1 = gson.toJson(save);

            // 保存后将消息发送至上游
            //jmsMessagingTemplate.convertAndSend("collpay.up" , s1);
            collpayProducer.sendMessage("collpay.up" , s1);
            return "交易处理中";
        }
        return "交易失败";
    }

    /**
     * 监听队列发送的消息 , 从队列中获取请求参数向上发送请求
     * @param collpay 获取请求参数
     * @return
     */
    @JmsListener(destination = "collpay.up")
    public void collpayToUp(String collpay) throws Exception {
        Gson gson = new Gson();
        CollpayInfo collpayInfo = gson.fromJson(collpay, CollpayInfo.class);

        //设置上游服务商号及机构号
        collpayInfo.setSp_id("1000");
        collpayInfo.setMch_id("100001000000000001");

        //将CollpayInfo转为json，再转map后进行操作
        String collpayInfoToJson = gson.toJson(collpayInfo);
        Map collpayInfoToMap = gson.fromJson(collpayInfoToJson, Map.class);

        collpayInfoToMap.remove("down_sp_id");
        collpayInfoToMap.remove("down_mch_id");
        collpayInfoToMap.remove("sign");
        //对上交易信息进行签名
        collpayInfoToMap.put("sign", SignUtils.sign(collpayInfoToMap, signKey));
        //AES加密操作
        collpayInfoToMap.replace("card_name", AesUtils.aesEn((String)collpayInfoToMap.get("card_name"), aesKey));
        collpayInfoToMap.replace("card_no", AesUtils.aesEn((String)collpayInfoToMap.get("card_no"), aesKey));
        collpayInfoToMap.replace("id_no", AesUtils.aesEn((String)collpayInfoToMap.get("id_no"), aesKey));
        collpayInfoToMap.replace("cvv2", AesUtils.aesEn((String)collpayInfoToMap.get("cvv2"), aesKey));
        collpayInfoToMap.replace("card_valid_date", AesUtils.aesEn((String)collpayInfoToMap.get("card_valid_date"), aesKey));
        collpayInfoToMap.replace("bank_mobile", AesUtils.aesEn((String)collpayInfoToMap.get("bank_mobile"), aesKey));

        //发送请求
        String responseInfo = HttpUtil.post(methodUrl, collpayInfoToMap, 6000);
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

        // 向上游请求成功时 , 存数据库
        if("SUCCESS".equals(response.getStatus())){
            collpayInfoRespository.save(collpayInfo);
        }

        // 上游处理订单状态为成功或失败时 , 通知给下游
        if (!("PROCESSING".equals(collpayInfo.getTrade_state()))){
            noticeService.noticeForDown(collpayInfo);
        }
        //return responseInfo;
    }

}
