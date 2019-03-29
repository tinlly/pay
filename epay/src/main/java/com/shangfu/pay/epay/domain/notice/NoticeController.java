package com.shangfu.pay.epay.domain.notice;

import cn.hutool.crypto.asymmetric.RSA;
import com.shangfu.pay.epay.domain.util.RSAUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by tinlly to 2019/3/25
 * Package for com.shangfu.pay.epay.domain.notice
 */
@RestController
public class NoticeController {

    //http://192.168.88.239:9002/shangfu/easypay/notice
    @PostMapping("/easypay/notice")
    public String noticeTest(@RequestBody String body){
        System.out.println("有通知了，解密为 : " + body);
        return "SUCCESS";
    }

}
