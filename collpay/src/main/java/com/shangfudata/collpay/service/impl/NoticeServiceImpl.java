package com.shangfudata.collpay.service.impl;

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
    public String notice(String out_trade_no) throws Exception{
        Gson gson = new Gson();
        //获得当前订单号的订单信息
        CollpayInfo collpayInfo = collpayInfoRespository.findByOutTradeNo(out_trade_no);
        //拿到订单信息中的下游机构号，再拿密钥
        String down_sp_id = collpayInfo.getDown_sp_id();
        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);

        //获取公钥
        String down_pub_key = downSpInfo.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        //获取私钥
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);

        //私钥签名
        String s = gson.toJson(collpayInfo);
        collpayInfo.setSign(RSAUtils.sign(s,rsaPrivateKey));

        //公钥加密
        collpayInfo.setCard_name(RSAUtils.publicKeyEncrypt(collpayInfo.getCard_name(), rsaPublicKey));
        collpayInfo.setCard_no(RSAUtils.publicKeyEncrypt(collpayInfo.getCard_no(), rsaPublicKey));
        collpayInfo.setId_no(RSAUtils.publicKeyEncrypt(collpayInfo.getId_no(), rsaPublicKey));
        collpayInfo.setBank_mobile(RSAUtils.publicKeyEncrypt(collpayInfo.getBank_mobile(), rsaPublicKey));
        collpayInfo.setCvv2(RSAUtils.publicKeyEncrypt(collpayInfo.getCvv2(), rsaPublicKey));
        collpayInfo.setCard_valid_date(RSAUtils.publicKeyEncrypt(collpayInfo.getCard_valid_date(), rsaPublicKey));

        String collpayInfotoJson = gson.toJson(collpayInfo);

        return collpayInfotoJson;
    }
}
