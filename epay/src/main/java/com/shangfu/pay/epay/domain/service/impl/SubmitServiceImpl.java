package com.shangfu.pay.epay.domain.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfu.pay.epay.domain.dao.EasypayInfoRespository;
import com.shangfu.pay.epay.domain.entity.EasypayInfo;
import com.shangfu.pay.epay.domain.service.SubmitService;
import com.shangfu.pay.epay.domain.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SubmitServiceImpl implements SubmitService {

    @Autowired
    EasypayInfoRespository easypayInfoRespository;

//    String methodUrl = "http://192.168.88.65:8888/gate/epay/epsubmit";
    String methodUrl = "http://testapi.shangfudata.com/gate/epay/epsubmit";
    String signKey = "00000000000000000000000000000000";

    @Override
    public String submit(String sumbitInfoToJson) {
        System.out.println("提交信息----："+sumbitInfoToJson);
        Gson gson = new Gson();
        Map sumbitInfoToMap = gson.fromJson(sumbitInfoToJson, Map.class);
        //感觉下面这行可以不要
        String out_trade_no = (String)sumbitInfoToMap.get("out_trade_no");
//        sumbitInfoToMap.replace("nonce_str","123456789");
        sumbitInfoToMap.put("sign", SignUtils.sign(sumbitInfoToMap, signKey));
        String responseInfo = HttpUtil.post(methodUrl, sumbitInfoToMap, 12000);

        EasypayInfo response = gson.fromJson(responseInfo, EasypayInfo.class);
        System.out.println("快捷验证码提交响应信息"+response);
        //if("SUCCESS".equals(response.getTrade_state())){
            String trade_state = response.getTrade_state();
            System.out.println("交易状态："+trade_state);
        String err_code = response.getErr_code();
        System.out.println("状态代码："+err_code);
        String err_msg = response.getErr_msg();
        System.out.println("状态说明："+err_msg);
            easypayInfoRespository.updateTradeState(trade_state,err_code,err_msg,out_trade_no);
        //}

//        // 上游处理订单状态为成功或失败时 , 通知给下游
//        if (!("PROCESSING".equals(response.getTrade_state()))){
//            noticeService.noticeForDown(collpayInfo);
//        }
        return responseInfo;
    }
}
