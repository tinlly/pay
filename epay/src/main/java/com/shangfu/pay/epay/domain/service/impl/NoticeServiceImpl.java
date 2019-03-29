package com.shangfu.pay.epay.domain.service.impl;

import cn.hutool.http.HttpRequest;
import com.google.gson.Gson;
import com.shangfu.pay.epay.domain.dao.DownSpInfoRespository;
import com.shangfu.pay.epay.domain.dao.EasypayInfoRespository;
import com.shangfu.pay.epay.domain.entity.DownSpInfo;
import com.shangfu.pay.epay.domain.entity.EasypayInfo;
import com.shangfu.pay.epay.domain.entity.NoticeInfo;
import com.shangfu.pay.epay.domain.service.NoticeService;
import com.shangfu.pay.epay.domain.util.GsonUtils;
import com.shangfu.pay.epay.domain.util.RSAUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;


@Service
public class NoticeServiceImpl implements NoticeService {

        // 通知地址
//        private String noticeURL = "http://localhost:9001/shangfu/collpay/notice";
        private String noticeURL = "http://192.168.88.239:9002/shangfu/easypay/notice";

        @Autowired
        EasypayInfoRespository easypayInfoRespository;

        @Autowired
        DownSpInfoRespository downSpInfoRespository;


    /**
     * 通知任务
     * 获取所有的订单
     * 判断 notice_status 状态
     * 将完成的订单通知到下游
     */
    @Scheduled(fixedDelay = 30000)
    @Async
    public void noticeTask() {
        // 获取所有的订单
        List<EasypayInfo> all = easypayInfoRespository.findAll();
        for (EasypayInfo easyPayOrder : all) {
            // 将交易成功且未通知的订单通知给下游
            if ("SUCCESS".equals(easyPayOrder.getTrade_state()) && !("true".equals(easyPayOrder.getNotice_status()))) {

                    try {
                        noticeForDown(easyPayOrder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            }
        }
    }

        /**
         * 向下游发送通知消息
         */
        @Override
        public void noticeForDown(EasypayInfo collpayInfo) throws Exception {
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

            //对象转JSON
            String content = gson.toJson(noticeInfo);

            // 签名加密
            noticeInfo.setSign(RSAUtils.sign(content , rsaPrivateKey));
            Map map = gson.fromJson(content, Map.class);

            String content1 = gson.toJson(noticeInfo);

            //输出通知信息
            System.out.println("给下游的通知为："+content1);

            // 通知次数
            int notice_count = 1;
            // 发送通知结果
            String body = null;

            // 当通知发送失败 或 通知次数达到 6 次
            while (!("SUCCESS".equals(body)) || notice_count != 7){
                body = HttpRequest.post(noticeURL).form(map).execute().body();
                notice_count++;
            }
            // 更改通知状态 : 代表已通知
            easypayInfoRespository.updateNoticeStatus("true",collpayInfo.getOut_trade_no());
        }

}
