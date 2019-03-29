package com.shangfu.pay.epay.domain.service;

import com.shangfu.pay.epay.domain.entity.EasypayInfo;

/**
 * Created by tinlly to 2019/3/25
 * Package for com.shangfu.pay.collpay.domain.service
 */
public interface NoticeService {

    void noticeForDown(EasypayInfo collpayInfo) throws Exception;

}
