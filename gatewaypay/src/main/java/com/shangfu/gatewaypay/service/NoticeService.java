package com.shangfu.gatewaypay.service;

public interface NoticeService {

    String Upnotice(String outTradeNo, String tradeState);

    void ToDown(String destinationName, String NoticeInfoToJson);
}
