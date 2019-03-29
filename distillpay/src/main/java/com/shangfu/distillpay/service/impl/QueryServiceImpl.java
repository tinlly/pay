package com.shangfu.distillpay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfu.distillpay.dao.DistillpayInfoRepository;
import com.shangfu.distillpay.entity.DistillpayInfo;
import com.shangfu.distillpay.entity.QueryInfo;
import com.shangfu.distillpay.service.QueryService;
import com.shangfu.distillpay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class QueryServiceImpl implements QueryService {



    @Autowired
    DistillpayInfoRepository distillpayInfoRepository;

    String queryUrl = "http://testapi.shangfudata.com/gate/spsvr/order/qry";
    String signKey = "36D2F03FA9C94DCD9ADE335AC173CCC3";

    String balanceqryUrl = "http://testapi.shangfudata.com/gate/rtp/balance/qry";

    /**
     * 向上查询（轮询方式）
     */
    //@Scheduled(cron = "*/30 * * * * ?")
    public void upQuery(){
        List<DistillpayInfo> all = this.distillpayInfoRepository.findAll();
        for (DistillpayInfo distillpayInfo : all){

            if ("SUCCESS".equals(distillpayInfo.getStatus())){
                Gson gson = new Gson();
                QueryInfo queryInfo = new QueryInfo();
                queryInfo.setMch_id(distillpayInfo.getMch_id());
                queryInfo.setOut_trade_no(distillpayInfo.getOut_trade_no());
                queryInfo.setNonce_str(distillpayInfo.getNonce_str());

                //将queryInfo转为json，再转map
                String query = gson.toJson(queryInfo);
                Map queryMap = gson.fromJson(query, Map.class);

                queryInfo.setSign(SignUtils.sign(queryMap,signKey));

                //将签名后的queryInfo再转json，map
                String query1 = gson.toJson(queryInfo);
                Map queryMap1 = gson.fromJson(query1, Map.class);

                //查询
                String post = HttpUtil.post(queryUrl, queryMap1, 6000);
//                System.out.println("返回的响应："+post);

                //使用一个新的UpdistillpayInfo对象，接收响应参数
                DistillpayInfo responseInfo = gson.fromJson(post, DistillpayInfo.class);
//                System.out.println(responseInfo);
                //如果订单状态发生改变
                if (!(responseInfo.getTrade_state().equals(distillpayInfo.getTrade_state()))){
                    //得到交易状态信息
                    String trade_state = responseInfo.getTrade_state();
                    //System.out.println("交易状态:"+trade_state);
                    String err_code = responseInfo.getErr_code();
                    //System.out.println("交易码:"+err_code);
                    String err_msg = responseInfo.getErr_msg();
                    //System.out.println("交易信息:"+err_msg);
                    //得到订单号
                    String out_trade_no = distillpayInfo.getOut_trade_no();

                    String notice_status = "true";

                    //根据订单号，更新数据库交易信息表
                    distillpayInfoRepository.updateByoutTradeNo(trade_state,err_code,err_msg,out_trade_no);

                }

            }

        }

    }






//    /**
//     * 向上查询（通过controller）
//     */
//    public void upQuery(DistillpayInfo distillpayInfo){
//        Gson gson = new Gson();
//        QueryInfo queryInfo = new QueryInfo();
//        queryInfo.setMch_id(distillpayInfo.getMch_id());
//        queryInfo.setOut_trade_no(distillpayInfo.getOut_trade_no());
//        queryInfo.setNonce_str(distillpayInfo.getNonce_str());
//
//        //将queryInfo转为json，再转map
//        String query = gson.toJson(queryInfo);
//        Map queryMap = gson.fromJson(query, Map.class);
//
//        queryInfo.setSign(SignUtils.sign(queryMap,signKey));
//
//        //将签名后的queryInfo再转json，map
//        String query1 = gson.toJson(queryInfo);
//        Map queryMap1 = gson.fromJson(query1, Map.class);
//
//        //查询
//        String post = HttpUtil.post(queryUrl, queryMap1, 6000);
//        System.out.println("返回的响应："+post);
//
//        DistillpayInfo distillpayInfo1 = gson.fromJson(post, DistillpayInfo.class);
////        System.out.println(distillpayInfo1);
//
//    }


    /**
     * 向上查询余额
     */
//    @Scheduled(cron = "*/10 * * * * ?")
    public DistillpayInfo upbalanceQuery(DistillpayInfo distillpayInfo1){
        String mch_id = distillpayInfo1.getMch_id();
        //这样查到的数据有多条，报错
//        DistillpayInfo distillpayInfo = distillpayInfoRepository.findBymchId(mch_id);
        DistillpayInfo distillpayInfo = distillpayInfoRepository.findByOutTradeNo("1553583029654");
        Gson gson = new Gson();
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setMch_id(distillpayInfo.getMch_id());
        queryInfo.setSp_id(distillpayInfo.getSp_id());
        queryInfo.setNonce_str(distillpayInfo.getNonce_str());

        //将queryInfo转为json，再转map
        String query = gson.toJson(queryInfo);
        Map queryMap = gson.fromJson(query, Map.class);

        queryInfo.setSign(SignUtils.sign(queryMap,signKey));

        //将签名后的queryInfo再转json，map
        String query1 = gson.toJson(queryInfo);
        Map queryMap1 = gson.fromJson(query1, Map.class);

        //查询
        String post = HttpUtil.post(balanceqryUrl, queryMap1, 6000);
        System.out.println("返回的响应："+post);
        return distillpayInfo;

    }


    /**
     * 下游来查询
     */
    public DistillpayInfo downQuery(DistillpayInfo distillpayInfo){
        String out_trade_no = distillpayInfo.getOut_trade_no();
        DistillpayInfo byOutTradeNo = distillpayInfoRepository.findByOutTradeNo(out_trade_no);
        return  byOutTradeNo;
    }

}
