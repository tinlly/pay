package com.shangfudata.easypay.service;

import java.util.Map;

public interface NoticeService {

    String Upnotice(Map map);

    void noticeDown(String message) throws Exception;

}
