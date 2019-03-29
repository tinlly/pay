package com.shangfudata.distillpay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.distillpay.dao.DistillpayInfoRespository;
import com.shangfudata.distillpay.dao.DownSpInfoRespository;
import com.shangfudata.distillpay.entity.DistillpayInfo;
import com.shangfudata.distillpay.entity.DownSpInfo;
import com.shangfudata.distillpay.jms.DistillpaySenderService;
import com.shangfudata.distillpay.service.DistillpayService;
import com.shangfudata.distillpay.util.AesUtils;
import com.shangfudata.distillpay.util.DataValidationUtils;
import com.shangfudata.distillpay.util.RSAUtils;
import com.shangfudata.distillpay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DistillpayServiceImpl implements DistillpayService {

    String methodUrl = "http://testapi.shangfudata.com/gate/rtp/distillpay";
    String signKey = "36D2F03FA9C94DCD9ADE335AC173CCC3";
    String aesKey = "45FBC053B1913EE83BE7C2801B263F3F";

    @Autowired
    DownSpInfoRespository downSpInfoRespository;
    @Autowired
    DistillpayInfoRespository distillpayInfoRespository;
    @Autowired
    DistillpaySenderService distillpaySenderService;

    public String downDistillpay(String distillpayInfoToJson) throws Exception{
        Map<String,String> responseMap = new HashMap();
        Gson gson = new Gson();

        Map map = gson.fromJson(distillpayInfoToJson, Map.class);

        DataValidationUtils builder = DataValidationUtils.builder();

        String nullValid = builder.isNullValid(map);
        if(!("".equals(nullValid))){
            responseMap.put("status","FAIL");
            responseMap.put("message",nullValid);
            return gson.toJson(responseMap);
        }


        String sign = (String)map.remove("sign");
        String s = gson.toJson(map);

        //下游传递上来的机构id，签名信息
        DistillpayInfo distillpayInfo = gson.fromJson(distillpayInfoToJson, DistillpayInfo.class);
        String down_sp_id = distillpayInfo.getDown_sp_id();

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);
        //拿到我自己（平台）的密钥(私钥)
        String my_pri_key = downSpInfo.get().getMy_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(my_pri_key);
        //拿到下游给的密钥(公钥)
        String down_pub_key = downSpInfo.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        //公钥验签
        if (RSAUtils.doCheck(s, sign, rsaPublicKey)){
            //私钥解密字段
            distillpayInfo.setCard_name(RSAUtils.privateKeyDecrypt(distillpayInfo.getCard_name(), rsaPrivateKey));
            distillpayInfo.setCard_no(RSAUtils.privateKeyDecrypt(distillpayInfo.getCard_no(), rsaPrivateKey));
            distillpayInfo.setId_no(RSAUtils.privateKeyDecrypt(distillpayInfo.getId_no(), rsaPrivateKey));

            // 效验数据
            builder.processDistillPayException(distillpayInfo , map);
            // 效验失败返回响应数据
            if(!(map.get("status").equals(""))){
                return gson.toJson(map);
            }

            distillpayInfoRespository.save(distillpayInfo);

            String dispayInfoToJson = gson.toJson(distillpayInfo);
            distillpaySenderService.sendMessage("distillpayinfo.test",dispayInfoToJson);

            // 响应数据
            responseMap.put("sp_id",distillpayInfo.getDown_sp_id());
            responseMap.put("mch_id",distillpayInfo.getDown_mch_id());
            responseMap.put("status", "SUCCESS");
            responseMap.put("trade_state", "正在处理中");
            return gson.toJson(responseMap);
        }
        responseMap.put("status", "FAIL");
        responseMap.put("trade_state", "签名错误");
        return gson.toJson(responseMap);
    }

    /**
     * 向上交易方法
     * @param distillpayInfoToJson
     * @return
     * 1.设置上游机构号和商户号
     * 2.删除下游机构号和商户号以及签名
     * 3.向上签名，加密，发送请求
     * 4.收到响应信息，存入传上来的collpay对象
     * 5.判断，保存数据库
     */
    @JmsListener(destination = "distillpayinfo.test")
    public void distillpayToUp(String distillpayInfoToJson){
        Gson gson = new Gson();

        Map distillpayInfoToMap = gson.fromJson(distillpayInfoToJson, Map.class);

        //设置上游服务商号及机构号
        distillpayInfoToMap.put("sp_id","1000");
        distillpayInfoToMap.put("mch_id","100050000000363");

        //将json串转为对象，便于存储数据库
        String s = gson.toJson(distillpayInfoToMap);
        DistillpayInfo distillpayInfo = gson.fromJson(s,DistillpayInfo.class);

        //移除下游信息
        distillpayInfoToMap.remove("down_sp_id");
        distillpayInfoToMap.remove("down_mch_id");
        distillpayInfoToMap.remove("sign");
        //对上交易信息进行签名
        distillpayInfoToMap.put("sign", SignUtils.sign(distillpayInfoToMap, signKey));
        //AES加密操作
        distillpayInfoToMap.replace("card_name", AesUtils.aesEn((String)distillpayInfoToMap.get("card_name"), aesKey));
        distillpayInfoToMap.replace("card_no", AesUtils.aesEn((String)distillpayInfoToMap.get("card_no"), aesKey));
        distillpayInfoToMap.replace("id_no", AesUtils.aesEn((String)distillpayInfoToMap.get("id_no"), aesKey));

        //发送请求
        String responseInfo = HttpUtil.post(methodUrl, distillpayInfoToMap, 12000);
        //获取响应信息，并用一个新CollpayInfo对象装下这些响应信息
        DistillpayInfo response = gson.fromJson(responseInfo, DistillpayInfo.class);

        //将响应信息存储到当前downCollpayInfo及UpCollpayInfo请求交易完整信息中
        distillpayInfo.setTrade_state(response.getTrade_state());
        distillpayInfo.setStatus(response.getStatus());
        distillpayInfo.setCode(response.getCode());
        distillpayInfo.setMessage(response.getMessage());
        distillpayInfo.setCh_trade_no(response.getCh_trade_no());
        distillpayInfo.setErr_code(response.getErr_code());
        distillpayInfo.setErr_msg(response.getErr_msg());

        if("SUCCESS".equals(response.getStatus())){
            //将订单信息表存储数据库
            distillpayInfoRespository.save(distillpayInfo);
        }

        // 向下通知上游订单处理结果
    }


}
