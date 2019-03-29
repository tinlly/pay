package com.shangfudata.gatewaypay.service;

import java.util.Map;

public interface NoticeService {

    //String Upnotice(String outTradeNo, String tradeState);

    String Upnotice(Map map);

    void noticeDown(String message) throws Exception;

}
