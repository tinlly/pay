package com.shangfu.pay.epay.domain.entity;

import lombok.Data;

/**
 * Created by tinlly to 2019/3/22
 * Package for com.shangfu.pay.collpay.domain.entity
 */
@Data
public class NoticeInfo {

    /**
     * 通知消息实体类
     */
    private String sp_id;               // 服务商号

    private String mch_id;              // 商户号

    private String out_trade_no;         // 商户订单号

    private String trade_status;         // 订单状态

    private String err_msg;             // 订单处理结果

    private String total_fee;           // 交易金额

    private String nonce_str;           // 随机字符串

    private String sign;                // 签名

}