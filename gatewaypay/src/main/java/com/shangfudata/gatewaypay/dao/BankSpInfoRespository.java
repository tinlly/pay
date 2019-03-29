package com.shangfudata.gatewaypay.dao;

import com.shangfudata.gatewaypay.entity.BankSpInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.io.Serializable;

@Repository
public interface BankSpInfoRespository extends JpaRepository<BankSpInfo,String>, JpaSpecificationExecutor<BankSpInfo>, Serializable {

}
