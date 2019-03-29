package com.shangfu.pay.collpay.domain.service.impl;

import cn.hutool.http.HttpRequest;
import com.google.gson.Gson;
import com.shangfu.pay.collpay.domain.dao.CollpayInfoRespository;
import com.shangfu.pay.collpay.domain.dao.DownSpInfoRespository;
import com.shangfu.pay.collpay.domain.entity.CollpayInfo;
import com.shangfu.pay.collpay.domain.entity.DownSpInfo;
import com.shangfu.pay.collpay.domain.entity.NoticeInfo;
import com.shangfu.pay.collpay.domain.service.NoticeService;
import com.shangfu.pay.collpay.domain.util.GsonUtils;
import com.shangfu.pay.collpay.domain.util.RSAUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;

/**
 * Created by tinlly to 2019/3/24
 * Package for com.shangfu.pay.collpay.domain.service
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    // 通知地址
    private String noticeURL = "http://localhost:9001/shangfu/collpay/notice";

    @Autowired
    CollpayInfoRespository collpayInfoRespository;

    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    /**
     * 向下游发送通知消息
     */
    public void noticeForDown(CollpayInfo collpayInfo) throws Exception {
        Gson gson = GsonUtils.getGson();

        System.out.println("downSpId " + collpayInfo.getDown_sp_id());

        Optional<DownSpInfo> byId = downSpInfoRespository.findById(collpayInfo.getDown_sp_id());

        //拿到密钥(私钥)
        String down_pri_key = byId.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);
        //拿到密钥(公钥)
        String down_pub_key = byId.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        // 封装通知结果
        NoticeInfo noticeInfo = new NoticeInfo();
        noticeInfo.setSp_id(collpayInfo.getSp_id());
        noticeInfo.setMch_id(collpayInfo.getMch_id());
        noticeInfo.setOut_trade_no(collpayInfo.getOut_trade_no());
        noticeInfo.setTrade_status(collpayInfo.getTrade_state());
        noticeInfo.setTotal_fee(collpayInfo.getTotal_fee());
        noticeInfo.setNonce_str(RSAUtils.publicKeyEncrypt(collpayInfo.getNonce_str() , rsaPublicKey));

        String content = gson.toJson(noticeInfo);

        // 签名加密
        noticeInfo.setSign(RSAUtils.sign(content , rsaPrivateKey));
        Map map = gson.fromJson(content, Map.class);

        // 通知次数
        int notice_count = 1;
        // 发送通知结果
        String body = null;

        // 当通知发送失败 或 通知次数达到 5 次
        while (!("SUCCESS".equals(body)) || notice_count != 5){
            body = HttpRequest.post(noticeURL).form(map).execute().body();
            notice_count++;
        }
        // 更改通知状态 : 代表已通知
        collpayInfoRespository.updateByNoticeStatus(collpayInfo.getOut_trade_no() , "true");
    }

}