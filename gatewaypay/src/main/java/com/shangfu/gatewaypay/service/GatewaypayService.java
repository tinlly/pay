package com.shangfu.gatewaypay.service;


public interface GatewaypayService {

    String downGatewaypay(String gatewaypayInfoToJson) throws Exception;

    String gatewaypayToUp(String gatewaypayInfoToJson);

}
