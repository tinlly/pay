package com.shangfu.pay.collpay.domain.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfu.pay.collpay.domain.dao.CollpayInfoRespository;
import com.shangfu.pay.collpay.domain.entity.CollpayInfo;
import com.shangfu.pay.collpay.domain.entity.QueryInfo;
import com.shangfu.pay.collpay.domain.service.QueryService;
import com.shangfu.pay.collpay.domain.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class QueryServiceImpl implements QueryService {


    String queryUrl = "http://testapi.shangfudata.com/gate/spsvr/order/qry";
    String signKey = "00000000000000000000000000000000";

    @Autowired
    CollpayInfoRespository collpayInfoRespository;

    /**
     * 向上查询
     */
    public Integer queryToUp(CollpayInfo collpayInfo) {
        Gson gson = new Gson();

        //判断处理状态为SUCCESS的才进行下一步操作
        //查询参数对象
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setMch_id(collpayInfo.getMch_id());
        queryInfo.setNonce_str(collpayInfo.getNonce_str());
        queryInfo.setOut_trade_no(collpayInfo.getOut_trade_no());

        //将queryInfo转为json，再转map
        String query = gson.toJson(queryInfo);
        Map queryMap = gson.fromJson(query, Map.class);
        //签名
        queryInfo.setSign(SignUtils.sign(queryMap, signKey));

        //将签名后的queryInfo再转json，map
        String query1 = gson.toJson(queryInfo);
        Map queryMap1 = gson.fromJson(query1, Map.class);

        //发送查询请求，得到响应信息
        String queryResponse = HttpUtil.post(queryUrl, queryMap1, 6000);

        //使用一个新的UpCollpayInfo对象，接收响应参数
        CollpayInfo responseInfo = gson.fromJson(queryResponse, CollpayInfo.class);
        System.out.println(responseInfo);

        //如果交易状态发生改变，那就更新。
        if (!(responseInfo.getTrade_state().equals(collpayInfo.getTrade_state()))) {
            //得到交易状态信息
            String trade_state = responseInfo.getTrade_state();
            System.out.println("交易状态:" + trade_state);
            String err_code = responseInfo.getErr_code();
            System.out.println("交易码:" + err_code);
            String err_msg = responseInfo.getErr_msg();
            System.out.println("交易信息:" + err_msg);
            //得到订单号
            String out_trade_no = collpayInfo.getOut_trade_no();
            System.out.println("订单号" + out_trade_no);

            //根据订单号，更新数据库交易信息表
            return collpayInfoRespository.updateByoutTradeNo(trade_state, err_code, err_msg, out_trade_no);
        }
        return 0;
    }


    /**
     * 下游查询方法
     *
     * @param collpayInfo
     */
//    @Cacheable(value = "collpay-info", key = "#order.outTradeNo", unless = "#result.tradeState eq 'PROCESSING'")
    public CollpayInfo downQuery(CollpayInfo collpayInfo) {
//        System.out.println("object > " + collpayInfo);
        String out_trade_no = collpayInfo.getOut_trade_no();
        CollpayInfo finalCollpayInfo = collpayInfoRespository.findByOutTradeNo(out_trade_no);
        return finalCollpayInfo;
    }


}
