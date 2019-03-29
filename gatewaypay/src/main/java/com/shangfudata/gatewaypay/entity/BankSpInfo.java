package com.shangfudata.gatewaypay.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 下游机构信息
 * 内含自有公私钥
 * 以及下游提供解密密钥
 */
@Data
@Entity
public class BankSpInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String bank_code;
    @Column
    private String bank_name;
    @Column
    private String self_pub_key;
    @Column
    private String self_pri_key;
    @Column
    private String down_sp_name;

}
