package com.shangfudata.easypay.service.impl;

import cn.hutool.http.HttpRequest;
import com.google.gson.Gson;
import com.shangfudata.easypay.dao.BankSpInfoRespository;
import com.shangfudata.easypay.dao.EasypayInfoRespository;
import com.shangfudata.easypay.entity.BankSpInfo;
import com.shangfudata.easypay.entity.EasypayInfo;
import com.shangfudata.easypay.mq.NoticeProducer;
import com.shangfudata.easypay.service.NoticeService;
import com.shangfudata.easypay.util.RSAUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import java.security.interfaces.RSAPrivateKey;
import java.util.Map;
import java.util.Optional;

@Service
public class NoticeServiceImpl implements NoticeService {
    
    @Autowired
    EasypayInfoRespository easypayInfoRespository;
    @Autowired
    BankSpInfoRespository bankSpInfoRespository;
    @Autowired
    NoticeProducer noticeProducer;

    public String Upnotice(Map map){
        Gson gson = new Gson();
        // 包装通知消息
        String s = gson.toJson(map);

        // 处理消息通知到下游
        noticeProducer.sendMessage("notice.down" , gson.toJson(s));

        String status = (String) map.get("trade_status");
        if("SUCCESS".equals(status)){
            return "SUCCESS";
        }
        return "FAIL";
    }

    /**
     * 向下通知
     * @param
     * @param message
     */
    @JmsListener(destination = "notice.down")
    @Override
    public void noticeDown(String message) throws Exception {
        Gson gson = new Gson();

        // 将对象转换为 Map
        Map map = gson.fromJson(message, Map.class);
        String s = noticeExecutor(map);
        Map map1 = gson.fromJson(s, Map.class);
        // 通知次数
        int notice_count = 1;
        // 发送通知结果
        String body = null;

        // 当通知发送失败 或 通知次数达到 5 次
        while (!("SUCCESS".equals(body)) && notice_count != 5){
            body = HttpRequest.post((String) map1.get("call_back_url")).form(map1).execute().body();
            notice_count++;
        }
        // 通知完成更改通知状态
        easypayInfoRespository.updateNoticeStatus((String) map.get("out_trade_no"), "true");
    }

    /**
     * 通知消息处理方法
     * @param map
     * @throws Exception
     */
    public String noticeExecutor(Map map) throws Exception {
        Gson gson = new Gson();

        Optional<BankSpInfo> downSpInfo = bankSpInfoRespository.findById((String) map.get("bank_code"));
        //拿到密钥(公钥)
        String pri_key = downSpInfo.get().getSelf_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(pri_key);

        Optional<EasypayInfo> outTradeNo = easypayInfoRespository.findById((String) map.get("out_trade_no"));
        EasypayInfo gatewayInfo = outTradeNo.get();
        gatewayInfo.getDown_sp_id();
        gatewayInfo.getMch_id();
        gatewayInfo.getTrade_state();
        gatewayInfo.getTotal_fee();
        gatewayInfo.getTrade_state();

        EasypayInfo gatewayInfo1 = new EasypayInfo();
        gatewayInfo1.setDown_mch_id(gatewayInfo.getDown_sp_id());
        gatewayInfo1.setMch_id(gatewayInfo.getMch_id());
        gatewayInfo1.setTrade_state(gatewayInfo.getTrade_state());
        gatewayInfo1.setTotal_fee(gatewayInfo.getTotal_fee());
        gatewayInfo1.setTrade_state(gatewayInfo.getTrade_state());
        gatewayInfo1.setNonce_str(gatewayInfo.getNonce_str());

        String s = gson.toJson(gatewayInfo1);
        gatewayInfo1.setSign(RSAUtils.sign(s , rsaPrivateKey));
        gatewayInfo1.setNotify_url(gatewayInfo.getNotify_url());
        return gson.toJson(gatewayInfo1);
    }

}
