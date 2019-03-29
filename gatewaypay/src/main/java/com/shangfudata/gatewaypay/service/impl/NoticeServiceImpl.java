package com.shangfudata.gatewaypay.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;

import com.shangfudata.gatewaypay.dao.BankSpInfoRespository;
import com.shangfudata.gatewaypay.dao.GatewaypayInfoRespository;
import com.shangfudata.gatewaypay.entity.BankSpInfo;
import com.shangfudata.gatewaypay.entity.GatewaypayInfo;
import com.shangfudata.gatewaypay.mq.NoticeProducer;
import com.shangfudata.gatewaypay.service.NoticeService;
import com.shangfudata.gatewaypay.util.RSAUtils;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.util.Map;
import java.util.Optional;

@Service
public class NoticeServiceImpl implements NoticeService {
    
    @Autowired
    GatewaypayInfoRespository gatewaypayInfoRespository;
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
        gatewaypayInfoRespository.updateNoticeStatus((String) map.get("out_trade_no"), "true");
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

        Optional<GatewaypayInfo> outTradeNo = gatewaypayInfoRespository.findById((String) map.get("out_trade_no"));
        GatewaypayInfo gatewayInfo = outTradeNo.get();
        gatewayInfo.getDown_sp_id();
        gatewayInfo.getMch_id();
        gatewayInfo.getTrade_state();
        gatewayInfo.getTotal_fee();
        gatewayInfo.getTrade_state();

        GatewaypayInfo gatewayInfo1 = new GatewaypayInfo();
        gatewayInfo1.setDown_mch_id(gatewayInfo.getDown_sp_id());
        gatewayInfo1.setMch_id(gatewayInfo.getMch_id());
        gatewayInfo1.setTrade_state(gatewayInfo.getTrade_state());
        gatewayInfo1.setTotal_fee(gatewayInfo.getTotal_fee());
        gatewayInfo1.setTrade_state(gatewayInfo.getTrade_state());
        gatewayInfo1.setNonce_str(gatewayInfo.getNonce_str());
        gatewayInfo1.setCall_back_url(gatewayInfo.getCall_back_url());

        String s = gson.toJson(gatewayInfo1);
        gatewayInfo1.setSign(RSAUtils.sign(s , rsaPrivateKey));
        gatewayInfo1.setNotify_url(gatewayInfo.getNotify_url());
        return gson.toJson(gatewayInfo1);
    }

    //@Override
    //public String Upnotice(String outTradeNo,String tradeState) {
    //    Gson gson = new Gson();
    //
    //    Optional<GatewaypayInfo> gatewaypayInfo = gatewaypayInfoRespository.findById(outTradeNo);
    //
    //    if(null == gatewaypayInfoRespository.findNoticeStatus(outTradeNo)){
    //        String easypayInfoToJson = gson.toJson(gatewaypayInfo);
    //        ToDown("gatewaypaytodown.notice",easypayInfoToJson);
    //        String noticeStatus = "true";
    //        gatewaypayInfoRespository.updateNoticeStatus(noticeStatus,outTradeNo);
    //    }
    //    return "SUCCESS";
    //}

}
