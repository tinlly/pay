package com.shangfudata.easypay.service;

import java.util.Map;

public interface NoticeService {

    String Upnotice(String outTradeNo,String tradeState);

    void ToDown(String destinationName,String NoticeInfoToJson);
}
