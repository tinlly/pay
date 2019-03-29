package com.shangfudata.distillpay.service.impl;

import cn.hutool.http.HttpRequest;
import com.google.gson.Gson;
import com.shangfudata.distillpay.dao.DistillpayInfoRespository;
import com.shangfudata.distillpay.dao.DownSpInfoRespository;
import com.shangfudata.distillpay.entity.DistillpayInfo;
import com.shangfudata.distillpay.entity.DownSpInfo;
import com.shangfudata.distillpay.service.NoticeService;
import com.shangfudata.distillpay.util.RSAUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

@Service
public class NoticeServiceImpl implements NoticeService {
    @Autowired
    DistillpayInfoRespository distillpayInfoRespository;
    
    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    @Override
    public void notice(String out_trade_no) throws Exception{
        Gson gson = new Gson();
        //获得当前订单号的订单信息
        DistillpayInfo distillpayInfo = distillpayInfoRespository.findByOutTradeNo(out_trade_no);
        //拿到订单信息中的下游机构号，再拿密钥
        String down_sp_id = distillpayInfo.getDown_sp_id();
        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);

        //获取私钥
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);

        //私钥签名
        String s = gson.toJson(distillpayInfo);
        distillpayInfo.setSign(RSAUtils.sign(s,rsaPrivateKey));

        //公钥加密
        distillpayInfo.setCard_name(distillpayInfo.getCard_name());
        distillpayInfo.setCard_no(distillpayInfo.getCard_no());
        distillpayInfo.setId_no(distillpayInfo.getId_no());

        String collpayInfotoJson = gson.toJson(distillpayInfo);

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
