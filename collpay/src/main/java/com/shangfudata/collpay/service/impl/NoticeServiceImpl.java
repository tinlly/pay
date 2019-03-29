package com.shangfudata.collpay.service.impl;

import cn.hutool.http.HttpRequest;
import com.google.gson.Gson;
import com.shangfudata.collpay.dao.CollpayInfoRespository;
import com.shangfudata.collpay.dao.DownSpInfoRespository;
import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.entity.DownSpInfo;
import com.shangfudata.collpay.service.NoticeService;
import com.shangfudata.collpay.util.RSAUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

@Service
public class NoticeServiceImpl implements NoticeService {
    @Autowired
    CollpayInfoRespository collpayInfoRespository;
    
    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    @Override
    public void notice(String out_trade_no) throws Exception{
        Gson gson = new Gson();
        //获得当前订单号的订单信息
        CollpayInfo collpayInfo = collpayInfoRespository.findByOutTradeNo(out_trade_no);
        //拿到订单信息中的下游机构号，再拿密钥
        String down_sp_id = collpayInfo.getDown_sp_id();
        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);

        //获取私钥
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);

        //公钥加密
        collpayInfo.setCard_name(collpayInfo.getCard_name());
        collpayInfo.setCard_no(collpayInfo.getCard_no());
        collpayInfo.setId_no(collpayInfo.getId_no());
        collpayInfo.setBank_mobile(collpayInfo.getBank_mobile());
        collpayInfo.setCvv2(collpayInfo.getCvv2());
        collpayInfo.setCard_valid_date(collpayInfo.getCard_valid_date());

        //私钥签名
        String s = gson.toJson(collpayInfo);
        collpayInfo.setSign(RSAUtils.sign(s,rsaPrivateKey));

        String collpayInfotoJson = gson.toJson(collpayInfo);

        // 通知计数
        int count = 0;
        // 通知结果
        String body = HttpRequest.post("").form(collpayInfotoJson).execute().body();
        while(body.equals("FAIL") && count != 5){
            body = HttpRequest.post("").form(collpayInfotoJson).execute().body();
            count++;
        }
    }
}