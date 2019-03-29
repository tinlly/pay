package com.shangfu.pay.epay.domain.service;

public interface EasypayService {

    String downEasypay(String easypayInfoToJson) throws Exception;

    String easypayToUp(String easypayInfoToJson);
}
