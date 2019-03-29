package com.shangfu.gatewaypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfu.gatewaypay.dao.DownSpInfoRespository;
import com.shangfu.gatewaypay.dao.GatewaypayInfoRespository;
import com.shangfu.gatewaypay.entity.DownSpInfo;
import com.shangfu.gatewaypay.entity.GatewaypayInfo;
import com.shangfu.gatewaypay.service.GatewaypayService;
import com.shangfu.gatewaypay.util.RSAUtils;
import com.shangfu.gatewaypay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;


@Service
public class GatewaypayServiceImpl implements GatewaypayService {

//    String methodUrl = "http://192.168.88.65:8888/gate/gw/apply";
    String methodUrl = "http://testapi.shangfudata.com/gate/gw/apply";
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

        System.out.println("####：：："+gatewaypayInfoToJson);

        Gson gson = new Gson();

        Map map = gson.fromJson(gatewaypayInfoToJson, Map.class);
        String sign = (String)map.remove("sign");
        System.out.println("sign::::"+sign);
        String s = gson.toJson(map);

        //下游传递上来的机构id，签名信息
        GatewaypayInfo gatewaypayInfo = gson.fromJson(gatewaypayInfoToJson, GatewaypayInfo.class);
        String down_sp_id = gatewaypayInfo.getDown_sp_id();

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);
        //拿到我自己（平台）的密钥(私钥)
        String my_pri_key = downSpInfo.get().getMy_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(my_pri_key);
        //拿到下游给的密钥(公钥)
        String down_pub_key = downSpInfo.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        //公钥验签
        boolean b = RSAUtils.doCheck(s, sign, rsaPublicKey);
        if (true == b){
            //gatewaypayInfo.setTrade_state("NOTPAY");
            gatewaypayInfoRespository.save(gatewaypayInfo);

            String gwpayInfoToJson = gson.toJson(gatewaypayInfo);
            //distillpaySenderService.sendMessage("distillpayinfo.test",dispayInfoToJson);

            return gatewaypayToUp(gwpayInfoToJson);
        }

        return "信息错误，交易失败";

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
        //System.out.println("添加签名后：：："+gatewaypayInfoToMap);
        //发送请求
        String responseInfo = HttpUtil.post(methodUrl, gatewaypayInfoToMap, 12000);

        boolean contains = responseInfo.contains("<html>");
        if(contains){
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
