package com.shangfudata.gatewaypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;

import com.shangfudata.gatewaypay.constant.CardType;
import com.shangfudata.gatewaypay.dao.DownSpInfoRespository;
import com.shangfudata.gatewaypay.dao.GatewaypayInfoRespository;
import com.shangfudata.gatewaypay.entity.DownSpInfo;
import com.shangfudata.gatewaypay.entity.GatewaypayInfo;
import com.shangfudata.gatewaypay.service.GatewaypayService;
import com.shangfudata.gatewaypay.util.DataValidationUtils;
import com.shangfudata.gatewaypay.util.RSAUtils;
import com.shangfudata.gatewaypay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;


import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class GatewaypayServiceImpl implements GatewaypayService {

    String methodUrl = "http://192.168.88.65:8888/gate/gw/apply";
    String signKey = "00000000000000000000000000000000";

    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    @Autowired
    GatewaypayInfoRespository gatewaypayInfoRespository;

    /**
     * 对下开放的网关交易
     * @param gatewaypayInfoToJson
     * @return
     * @throws Exception
     */
    public String downGatewaypay(String gatewaypayInfoToJson) throws Exception {
        Map responseMap = new HashMap();
        DataValidationUtils builder = DataValidationUtils.builder();
        Gson gson = new Gson();

        Map map = gson.fromJson(gatewaypayInfoToJson, Map.class);
        // 请求数据不为空验证
        String nullValid = builder.isNullValid(map);
        if(!(nullValid.equals(""))){
            responseMap.put("status" , "FAIL");
            responseMap.put("message" , nullValid);
            return gson.toJson(responseMap);
        }

        String sign = (String)map.remove("sign");
        String s = gson.toJson(map);

        //下游传递上来的机构id，签名信息
        GatewaypayInfo gatewaypayInfo = gson.fromJson(gatewaypayInfoToJson, GatewaypayInfo.class);

        // 数据合法性验证
        builder.processDistillPayException(gatewaypayInfo , responseMap);
        if(responseMap.get("status").equals("FAIL")){
            responseMap.put("sp_id" , gatewaypayInfo.getSp_id());
            responseMap.put("mch_id" , gatewaypayInfo.getDown_mch_id());
            return gson.toJson(responseMap);
        }
        String down_sp_id = gatewaypayInfo.getDown_sp_id();

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);
        //拿到我自己（平台）的密钥(私钥)
        String my_pri_key = downSpInfo.get().getMy_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(my_pri_key);
        //拿到下游给的密钥(公钥)
        String down_pub_key = downSpInfo.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        //公钥验签
        if (RSAUtils.doCheck(s, sign, rsaPublicKey)){

            if(gatewaypayInfo.getCard_type().equals(CardType.DEBIT)){
                // 借记卡处理
            }else if(gatewaypayInfo.getCard_type().equals(CardType.CREDIT)){
                // 贷记卡处理
            }

            gatewaypayInfoRespository.save(gatewaypayInfo);

            String gwpayInfoToJson = gson.toJson(gatewaypayInfo);

            return gatewaypayToUp(gwpayInfoToJson);
        }
        // 解密错误返回错误信息
        responseMap.put("status", "FAIL");
        responseMap.put("trade_state", "签名错误");
        return gson.toJson(responseMap);
    }

    /**
     * 向上的网关交易
     * @param gatewaypayInfoToJson
     * @return
     */
    public String gatewaypayToUp(String gatewaypayInfoToJson) {
        Gson gson = new Gson();

        Map gatewaypayInfoToMap = gson.fromJson(gatewaypayInfoToJson, Map.class);

        //设置上游服务商号及机构号
        // TODO: 2019/3/29 将一下内容替换为 > 根据 sp_id 查询数据库
        gatewaypayInfoToMap.put("sp_id","1000");
        gatewaypayInfoToMap.put("mch_id","100001000000000001");

        //将json串转为对象，便于存储数据库
        String s = gson.toJson(gatewaypayInfoToMap);
        GatewaypayInfo gatewaypayInfo = gson.fromJson(s,GatewaypayInfo.class);

        //移除下游信息
        gatewaypayInfoToMap.remove("down_sp_id");
        gatewaypayInfoToMap.remove("down_mch_id");
        gatewaypayInfoToMap.remove("sign");
        //对上交易信息进行签名
        gatewaypayInfoToMap.put("sign", SignUtils.sign(gatewaypayInfoToMap, signKey));
        //发送请求
        String responseInfo = HttpUtil.post(methodUrl, gatewaypayInfoToMap, 12000);

        // 判断是否为 html 标签
        if(responseInfo.contains("<html>")){
            gatewaypayInfo.setStatus("SUCCESS");
            gatewaypayInfo.setTrade_state("PROCESSING");
            gatewaypayInfoRespository.save(gatewaypayInfo);
        }else{
            GatewaypayInfo response = gson.fromJson(responseInfo, GatewaypayInfo.class);

            gatewaypayInfo.setTrade_state(response.getStatus());
            gatewaypayInfo.setStatus(response.getStatus());
            gatewaypayInfo.setCode(response.getCode());
            gatewaypayInfo.setMessage(response.getMessage());

            gatewaypayInfoRespository.save(gatewaypayInfo);
        }

        return responseInfo;
    }


}
