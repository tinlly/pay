package com.shangfudata.collpay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.collpay.dao.CollpayInfoRespository;
import com.shangfudata.collpay.dao.DownSpInfoRespository;
import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.entity.DownSpInfo;
import com.shangfudata.collpay.entity.QueryInfo;
import com.shangfudata.collpay.exception.NonceStrLengthException;
import com.shangfudata.collpay.service.NoticeService;
import com.shangfudata.collpay.service.QueryService;
import com.shangfudata.collpay.util.DataValidationUtils;
import com.shangfudata.collpay.util.RSAUtils;
import com.shangfudata.collpay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QueryServiceImpl implements QueryService {


    String queryUrl = "http://testapi.shangfudata.com/gate/spsvr/order/qry";
    String signKey = "00000000000000000000000000000000";


    @Autowired
    CollpayInfoRespository collpayInfoRespository;
    @Autowired
    NoticeService noticeService;
    @Autowired
    NoticeController noticeController;
    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    /**
     * 向上查询（轮询方法）
     */
    @Scheduled(cron = "*/60 * * * * ?")
    public void queryToUp() throws Exception {
        Gson gson = new Gson();

        //查询所有交易状态为PROCESSING的订单信息
        List<CollpayInfo> collpayInfoList = collpayInfoRespository.findByTradeState("PROCESSING");

        //遍历
        for (CollpayInfo collpayInfo : collpayInfoList) {
            //判断处理状态为SUCCESS的才进行下一步操作
            if ("SUCCESS".equals(collpayInfo.getStatus())) {
                //查询参数对象
                QueryInfo queryInfo = new QueryInfo();
                queryInfo.setMch_id(collpayInfo.getMch_id());
                queryInfo.setNonce_str(collpayInfo.getNonce_str());
                queryInfo.setOut_trade_no(collpayInfo.getOut_trade_no());

                //将queryInfo转为json，再转map
                String query = gson.toJson(queryInfo);
                Map queryMap = gson.fromJson(query, Map.class);
                queryMap.put("sign", SignUtils.sign(queryMap, signKey));

                //发送查询请求，得到响应信息
                String queryResponse = HttpUtil.post(queryUrl, queryMap, 6000);

                //使用一个新的UpCollpayInfo对象，接收响应参数
                CollpayInfo responseInfo = gson.fromJson(queryResponse, CollpayInfo.class);

                //如果交易状态发生改变，那就更新。
                if (!(responseInfo.getTrade_state().equals(collpayInfo.getTrade_state()))) {

                    //得到交易状态信息
                    String trade_state = responseInfo.getTrade_state();
                    String err_code = responseInfo.getErr_code();
                    String err_msg = responseInfo.getErr_msg();
                    //得到订单号
                    String out_trade_no = collpayInfo.getOut_trade_no();

                    String notice_status = "true";
                    //根据订单号，更新数据库交易信息表
                    collpayInfoRespository.updateByoutTradeNo(trade_state, err_code, err_msg, out_trade_no);

                    noticeController.notice(out_trade_no);

                    collpayInfoRespository.updateNoticeStatus(notice_status, out_trade_no);
                }
            }
        }
    }

    /**
     * 下游查询方法
     *
     * @param collpayInfoToJson
     */
    @Cacheable(value = "collpay", key = "#order.outTradeNo", unless = "#result.tradeState eq 'PROCESSING'")
    public String downQuery(String collpayInfoToJson) throws Exception {
        Gson gson = new Gson();
        CollpayInfo collpayInfo = gson.fromJson(collpayInfoToJson, CollpayInfo.class);

        String s = gson.toJson(collpayInfo);
        Map map = gson.fromJson(s, Map.class);
        // 处理请求不能为空
        DataValidationUtils builder = DataValidationUtils.builder();
        try {
            String nullValid = builder.isNullValid(map);
            if (!(nullValid.equals(""))) {
                CollpayInfo collpayInfo1 = new CollpayInfo();
                collpayInfo1.setStatus("FAIL");
                collpayInfo1.setErr_msg(nullValid);
                return gson.toJson(collpayInfo1);
            }
            builder.nonceStrValid((String) map.get("nonce_str"));
        } catch (NonceStrLengthException e) {
            CollpayInfo collpayInfo1 = new CollpayInfo();
            collpayInfo1.setStatus("FAIL");
            collpayInfo1.setErr_msg("随机字符串长度错误");
            return gson.toJson(collpayInfo1);
        }

        String out_trade_no = collpayInfo.getOut_trade_no();

        CollpayInfo finalCollpayInfo = collpayInfoRespository.findByOutTradeNo(out_trade_no);

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(finalCollpayInfo.getDown_sp_id());
        //拿到密钥(私钥)
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);
        //拿到密钥(公钥)
        String down_pub_key = downSpInfo.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        CollpayInfo collpayInfo1 = new CollpayInfo();

        collpayInfo1.setStatus(finalCollpayInfo.getStatus());
        collpayInfo1.setErr_msg(finalCollpayInfo.getErr_msg());
        collpayInfo1.setDown_mch_id(finalCollpayInfo.getDown_mch_id());
        collpayInfo1.setSp_id(finalCollpayInfo.getSp_id());
        collpayInfo1.setNonce_str(finalCollpayInfo.getNonce_str());
        collpayInfo1.setSign(RSAUtils.sign(gson.toJson(collpayInfo1), rsaPrivateKey));

        // 返回查询请求信息
        return gson.toJson(collpayInfo1);
    }

}
