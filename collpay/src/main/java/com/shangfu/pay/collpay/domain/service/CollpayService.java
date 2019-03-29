package com.shangfu.pay.collpay.domain.service;

import java.util.Map;

/**
 * Created by tinlly to 2019/3/25
 * Package for com.shangfu.pay.collpay.domain.service
 */
public interface CollpayService {

    String collpayToDown(Map<String , String> map) throws Exception;

    void collpayToUp(String collpay) throws Exception;

}
