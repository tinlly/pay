package com.shangfu.pay.collpay.domain.service;

import com.shangfu.pay.collpay.domain.entity.CollpayInfo;

/**
 * Created by tinlly to 2019/3/25
 * Package for com.shangfu.pay.collpay.domain.service
 */
public interface NoticeService {

    void noticeForDown(CollpayInfo collpayInfo) throws Exception;

}
