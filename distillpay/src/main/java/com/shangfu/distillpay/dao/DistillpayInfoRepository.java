package com.shangfu.distillpay.dao;

import com.shangfu.distillpay.entity.DistillpayInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

@Repository
public interface DistillpayInfoRepository extends JpaRepository<DistillpayInfo,String>, JpaSpecificationExecutor<DistillpayInfo>, Serializable {

    @Query("select c from DistillpayInfo c where c.out_trade_no =?1")
    DistillpayInfo findByOutTradeNo(String OutTradeNo);

    @Query("select c from DistillpayInfo c where c.mch_id =?1")
    DistillpayInfo findBymchId(String mchId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update DistillpayInfo d set d.trade_state =?1, d.err_code =?2, d.err_msg =?3 where d.out_trade_no =?4")
    void updateByoutTradeNo(String tradeState, String errCode, String errMsg, String OutTradeNo);
}
