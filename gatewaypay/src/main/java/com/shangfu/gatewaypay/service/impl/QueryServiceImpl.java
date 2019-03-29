package com.shangfu.gatewaypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfu.gatewaypay.dao.GatewaypayInfoRespository;
import com.shangfu.gatewaypay.entity.GatewaypayInfo;
import com.shangfu.gatewaypay.entity.QueryInfo;
import com.shangfu.gatewaypay.service.QueryService;
import com.shangfu.gatewaypay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class QueryServiceImpl implements QueryService {

//    String methodUrl = "http://testapi.shangfudata.com/gate/gw/apply";
    String queryUrl = "http://testapi.shangfudata.com/gate/spsvr/order/qry";
    String signKey = "00000000000000000000000000000000";

    @Autowired
    GatewaypayInfoRespository gatewaypayInfoRespository;

    /**
     * 向上查询（轮询方法）
     */
    @Scheduled(cron = "*/30 * * * * ?")
    public void queryToUp() throws Exception {

        Gson gson = new Gson();

        //查询所有交易状态为PROCESSING的订单信息
        List<GatewaypayInfo> distillpayInfoList = gatewaypayInfoRespository.findByTradeState("PROCESSING");

        //遍历
        for (GatewaypayInfo gatewaypayInfo : distillpayInfoList) {
            //System.out.println(distillpayInfo);
            //判断处理状态为SUCCESS的才进行下一步操作
            if ("SUCCESS".equals(gatewaypayInfo.getStatus())) {
                //if ("PROCESSING".equals(updistillpayInfo.getTrade_state())) {
                //查询参数对象
                QueryInfo queryInfo = new QueryInfo();
                queryInfo.setMch_id(gatewaypayInfo.getMch_id());
                queryInfo.setNonce_str(gatewaypayInfo.getNonce_str());
                queryInfo.setOut_trade_no(gatewaypayInfo.getOut_trade_no());

                //将queryInfo转为json，再转map
                String query = gson.toJson(queryInfo);
                Map queryMap = gson.fromJson(query, Map.class);
                //签名
                queryMap.put("sign", SignUtils.sign(queryMap, signKey));

                //发送查询请求，得到响应信息
                String queryResponse = HttpUtil.post(queryUrl, queryMap, 6000);
                System.out.println("查询到的响应信息："+queryResponse);
                //使用一个新的UpdistillpayInfo对象，接收响应参数
                GatewaypayInfo responseInfo = gson.fromJson(queryResponse, GatewaypayInfo.class);
                // System.out.println(responseInfo);
                //如果交易状态发生改变，那就更新。
                if (!(responseInfo.getTrade_state().equals(gatewaypayInfo.getTrade_state()))) {


                    //得到交易状态信息
                    String trade_state = responseInfo.getTrade_state();
                    String err_code = responseInfo.getErr_code();
                    String err_msg = responseInfo.getErr_msg();
                    //结算状态
                    String settle_state = responseInfo.getSettle_state();
                    //结算状态说明
                    String settle_state_desc = responseInfo.getSettle_state_desc();
                    String ch_trade_no = responseInfo.getCh_trade_no();

                    String out_trade_no = gatewaypayInfo.getOut_trade_no();
                    //System.out.println("订单号"+out_trade_no);


                    //String notice_status = "true";
                    //根据订单号，更新数据库交易信息表
                    gatewaypayInfoRespository.updateTradeState(trade_state, err_code, err_msg, settle_state, settle_state_desc, ch_trade_no, out_trade_no);

                }
                //}
            }
        }
    }


    /**
     * 下游查询方法
     * @param gatewaypayInfoToJson
     */
    //@Cacheable(value = "collpay", key = "#order.outTradeNo", unless = "#result.tradeState eq 'PROCESSING'")
    public String downQuery(String gatewaypayInfoToJson){
        Gson gson = new Gson();
        GatewaypayInfo gatewaypayInfo = gson.fromJson(gatewaypayInfoToJson, GatewaypayInfo.class);
        String out_trade_no = gatewaypayInfo.getOut_trade_no();

        GatewaypayInfo finalGatewaypayInfo = gatewaypayInfoRespository.findByOutTradeNo(out_trade_no);

        return gson.toJson(finalGatewaypayInfo);
    }


}
