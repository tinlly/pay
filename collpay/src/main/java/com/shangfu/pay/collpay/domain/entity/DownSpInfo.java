package com.shangfu.pay.collpay.domain.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 下游机构信息
 * 内含自有公私钥
 * 以及下游提供解密密钥
 */
@Data
@Entity
public class DownSpInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String down_sp_id;
    @Column
    private String down_sp_name;
    @Column
    private String my_pub_key;
    @Column
    private String my_pri_key;
    @Column
    private String down_pri_key;
    @Column
    private String down_pub_key;



}
