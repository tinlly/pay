package com.shangfudata.easypay.dao;

import com.shangfudata.easypay.entity.BankSpInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public interface BankSpInfoRespository extends JpaRepository<BankSpInfo,String>, JpaSpecificationExecutor<BankSpInfo>, Serializable {

}
