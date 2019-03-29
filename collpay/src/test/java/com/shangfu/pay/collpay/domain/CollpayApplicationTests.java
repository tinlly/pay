package com.shangfu.pay.collpay.domain;

import com.google.gson.Gson;
import com.shangfu.pay.collpay.domain.dao.CollpayInfoRespository;
import com.shangfu.pay.collpay.domain.dao.DownSpInfoRespository;
import com.shangfu.pay.collpay.domain.entity.CollpayInfo;
import com.shangfu.pay.collpay.domain.entity.DownSpInfo;
import com.shangfu.pay.collpay.domain.service.QueryService;
import com.shangfu.pay.collpay.domain.util.GsonUtils;
import com.shangfu.pay.collpay.domain.util.RSAUtils;
import lombok.extern.java.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.SpringRunner;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableScheduling
@Log
public class CollpayApplicationTests {

//    @Autowired
//    CollPayOrderService collpayService;

    @Autowired
    QueryService queryService;

//    @Autowired
//    DownSpInfoDao downSpInfoDao;

    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    @Autowired
    CollpayInfoRespository collpayInfoRespository;

    public String priKey = "MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEAqyNTxzNK3q3jqmtuykrg2a9m2aaYtjFJAvkaXQHPU8pvLMyFzJrOmrFOy6SdOnBlyuzLW1lp1d3tpqJ9CZ3dQwIDAQABAkEAjMKx1dZKbn14FGPo7FpKNsIeRkbQtIo1E0zwci9a5/7uutrecchYPpITYsncWSrHkotzO6B7UfTvAkpTAh5fAQIhAOk/HYmkRZLkANiYisAL/GMj/1PJlrUnDGY/25/4u387AiEAu9UrjzVLYiFTccTu3Ab7amMOkec6bZ+esGZttKhlSZkCIQCNoIdc9mRQhyWEX0uQxTZhNJBq3fMm2CkNRSUkg7HF1QIgLYkwVjeFXvTVVe94OL84lEIPdi+oaosX3Yv3bKSmaYECIQCmq/7+6EVgIx3JW4MzqKNzPWg4JGKGl131ID9HoMa4Lw==";
    public String pubKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKsjU8czSt6t46prbspK4NmvZtmmmLYxSQL5Gl0Bz1PKbyzMhcyazpqxTsuknTpwZcrsy1tZadXd7aaifQmd3UMCAwEAAQ==";

//    @Test
//    public void contextLoads() {
//        CollPayOrder collpay = new CollPayOrder();
//        //collpay.setSpId("1000");
//        //collpay.setMchId("100001000000000001");
//        collpay.setOutTradeNo(System.currentTimeMillis() + "");
//        collpay.setBody("test99999");
//        collpay.setTotalFee("3000");
//        collpay.setCardType("CREDIT");
//        collpay.setCardName("小鱼仔");
//        collpay.setCardNo("6222021001134258654");
//        collpay.setIdType("ID_CARD");
//        collpay.setIdNo("410781199004016952");
//        collpay.setBankMobile("12345678912");
//        collpay.setCvv2("123");
//        collpay.setCardValidDate("0318");
//        collpay.setNotifyUrl("http://192.168.168.168");
//        collpay.setNonceStr("123456789");
//
//        this.collpayService.sendData(collpay);
//    }

    //@Test
    //public void queryMap() throws Exception {
    //    //// 加密签名串
    //    //List<Key> keys = RsaEncryption.initKey();
    //    ////String decrypt = RsaEncryption.decrypt(sign.getBytes(CharsetUtil.UTF_8), (PrivateKey) keys.get(1));
    //    //Gson gson = GsonUtils.getGson();
    //    //CollPayOrder collPayOrder = new CollPayOrder();
    //    //collPayOrder.setMchId("100001000000000001");
    //    //collPayOrder.setNonceStr("123456789");
    //    //collPayOrder.setOutTradeNo("155246658650491111");
    //    //// 生成密文串
    //    //String s = "mch_id=" + collPayOrder.getMchId() + "&nonce_str=" + collPayOrder.getNonceStr() + "&out_trade_no=" + collPayOrder.getOutTradeNo() + "&key=" + 123456789;
    //    //this.log.info("密文串 > " + s);
    //    //
    //    //collPayOrder.setSign(RsaEncryption.encrypt(s, (PublicKey) keys.get(0)));
    //    //String s1 = gson.toJson(collPayOrder);
    //    //this.log.info("加密后的密文串 > " + s1);
    //    //queryCollpayService.queryCollpay(collPayOrder);
    //}
    //
    //@Test
    //public void test() throws InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, UnsupportedEncodingException {
    //    // Rsa 加密数据
    //    // 未初始化 私钥 公钥
    //    List<byte[]> keys = RsaEncryption.initKey();
    //    // 初始化 公钥 私钥
    //    List<Key> keys2 = RsaEncryption.codingKey(keys.get(0), keys.get(1));
    //
    //    // 数据库存储生成未初始化的 私钥 公钥
    //    // 私钥公钥 base64 转换 byte
    //    java.lang.String s2 = new java.lang.String(Base64.getEncoder().encode(keys.get(0)), CharsetUtil.UTF_8);
    //    System.out.println(" >>> " + s2);
    //    java.lang.String s21 = new java.lang.String(Base64.getEncoder().encode(keys.get(1)), CharsetUtil.UTF_8);
    //    System.out.println(" >>> " + s21);
    //
    //    // byte base64 转换 公钥私钥
    //    byte[] decode = Base64.getDecoder().decode(s2.getBytes(CharsetUtil.UTF_8));
    //    byte[] decode1 = Base64.getDecoder().decode(s21.getBytes(CharsetUtil.UTF_8));
    //
    //    List<Key> keys1 = RsaEncryption.codingKey(decode, decode1);
    //
    //
    //    PublicKey publicKey = (PublicKey) keys2.get(0);
    //
    //    java.lang.String encrypt = RsaEncryption.encrypt("123456789", publicKey);
    //    System.out.println("public key 加密 > " + encrypt);
    //
    //    List<Key> keys3 = RsaEncryption.codingKey(decode, decode1);
    //    // 解密
    //    java.lang.String decrypt = RsaEncryption.decrypt(encrypt.getBytes(CharsetUtil.UTF_8), (PrivateKey) keys3.get(1));
    //    System.out.println("private key 解密 > " + decrypt);
    //}

    /**
     * 发送 Rsa 加密数据
     */
    @Test
    public void testRsa() throws Exception {
        Gson gson = GsonUtils.getGson();

        // 初始化对象
        //DownCollpayInfo downCollpayInfo = new DownCollpayInfo();
        //downCollpayInfo.setDown_sp_id("1000");
        //downCollpayInfo.setDown_mch_id("77777");
        //downCollpayInfo.setOut_trade_no(System.currentTimeMillis() + "");
        //downCollpayInfo.setBody("test99999");
        //downCollpayInfo.setTotal_fee("3000");
        //downCollpayInfo.setCard_type("CREDIT");
        //downCollpayInfo.setCard_no("6222021001134258654");
        //downCollpayInfo.setId_type("ID_CARD");
        //downCollpayInfo.setId_no("410781199004016952");
        //downCollpayInfo.setBank_mobile("12345678912");
        //downCollpayInfo.setCvv2("123");
        //downCollpayInfo.setCard_valid_date("0318");
        //downCollpayInfo.setNonce_str("123456789");
        //downCollpayInfo.setCard_name("周黑鸭");

        //downCollpayInfo.setSign(SignUtils.sign(gson.fromJson(gson.toJson(downCollpayInfo) , Map.class) , "00000000000000000000000"));

        // 生成 公钥 密钥
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCQ4CioxpI14rKRPkRyWmiWAvZwb+HrROoSxSiHziH6S7tvpu2VokbSnklLeKYfz7C3htPatK4DQnuu5sv7F1A3fB+NZRR9oYq0+45UOmQ/doRCK5j4oyc9o1iO93EiHa4r7Kc6xARuyuweQU7UkModg37n5ikBHY7g+LS09GDWxwIDAQAB");
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJDgKKjGkjXispE+RHJaaJYC9nBv4etE6hLFKIfOIfpLu2+m7ZWiRtKeSUt4ph/PsLeG09q0rgNCe67my/sXUDd8H41lFH2hirT7jlQ6ZD92hEIrmPijJz2jWI73cSIdrivspzrEBG7K7B5BTtSQyh2DfufmKQEdjuD4tLT0YNbHAgMBAAECgYAUE5xO10XHxeStEA34MuMvdrWkGz0Zl3FArKXTPBOlVf1hmaZvCA/8fAb4OH39BpEcvch6FHPQ69OsBzvZTf/FmuXjY2T7RyFPUNhvpUb97XlvUlfsBa30RTIHIbbFbG6H+/jyY71abYG6JSD61qNgGkqWL0bhkk+/UVrhKqGNMQJBANh766KD6+LSzQqYIjR20Spw3ssegGoiHUXU2IlbS0GkEVSkj7mRbX+DFhQ+uwqBpxu8rQxdupy5Rcrt3ynx3Z0CQQCrUgx/owqcQayvvhaTlJsaM5zF6zW2D50XYBMBZYWWwWypdeOQ2uZhHEI36V7u6NRHjIwXYdRsKfQ4e4DKacqzAkEAr5i8xmLpaGAC70/9lr615Q4OuYVxNiWbxvPh/HBv97uETzr4VdZPkjmbJCrJ/rix+r6tQzWX0944bWVLOjO0xQJAR4735YBGeEn+RVHSwEX8Gw+f1hOO1cLjzNW2Woj7KaMVLhstuF2WiM1y40O5AXWC5XfRNBsKzTsg5U7A1sslfQJBANQBI6K2rHb6tRJb3+IeOt1iD4afXBvXLZUj7wYfNmdql8+rWs4ryPzW/MC7rySX/lT8AHzZEqMVD5GGFyGiHLc=");

        // Rsa 加密
        //downCollpayInfo.setCard_no(RSAUtils.publicKeyEncrypt(downCollpayInfo.getCard_no() , rsaPublicKey));
        //downCollpayInfo.setBank_mobile(RSAUtils.publicKeyEncrypt(downCollpayInfo.getBank_mobile() , rsaPublicKey));
        //downCollpayInfo.setId_no(RSAUtils.publicKeyEncrypt(downCollpayInfo.getId_no() , rsaPublicKey));
        //downCollpayInfo.setCvv2(RSAUtils.publicKeyEncrypt(downCollpayInfo.getCvv2() , rsaPublicKey));
        //downCollpayInfo.setCard_valid_date(RSAUtils.publicKeyEncrypt(downCollpayInfo.getCard_valid_date() , rsaPublicKey));

        // 签名串签名
        //String sign = RSAUtils.sign(gson.toJson(downCollpayInfo), rsaPrivateKey);
        //downCollpayInfo.setSign(sign);

        //String s = gson.toJson(downCollpayInfo);
        //System.out.println("请求参数 > " + s);
        //Map map = gson.fromJson(s, Map.class);

        //String body = cn.hutool.http.HttpRequest.post("http://localhost:9001/shangfu/collpay/test/testRsaP").form(map).execute().body();
        //String body = cn.hutool.http.HttpRequest.post("http://localhost:9001/shangfu/collpay/sendInfo").form(map).execute().body();
        //System.out.println("请求结果 > " + body);
    }

    /**
     * 签名验签测试方法
     */
    @Test
    public void testSign() throws Exception {
        String data = "中国银行";

        //加密用公钥加密，私钥解密
        //签名用私钥，验签用公钥
        //Optional<DownSpInfo> downSpInfo = this.downCollpayInfoDao.queryRsaKey("9999");
        //Optional<DownSpInfo> downSpInfo = this.downCollpayInfoDao.findById("9999");
        //DownSpInfo downSp = downSpInfoDao.findDownSpInfoBySpId("1000");

        //获取密钥字符串
        //String down_pri_key1 = downSp.getDown_pri_key();
        //String down_pub_key = downSp.getDown_pub_key();
        //生成密钥对
        //RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key1);
        //RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        //公钥加密
        //String publicEncrypt = RSAUtils.publicKeyEncrypt(data, rsaPublicKey);
        //System.out.println("公钥加密："+publicEncrypt);

        //私钥解密
        //String privateKeyDecrypt = RSAUtils.privateKeyDecrypt(publicEncrypt, rsaPrivateKey);
        //System.out.println("私钥解密："+privateKeyDecrypt);

        //私钥签名
        //String sign1 = RSAUtils.sign(data, rsaPrivateKey);
        //System.out.println(sign1);
        //公钥验签
        //boolean b1 = RSAUtils.doCheck(data, sign1, rsaPublicKey);
        //System.out.println(b1);
    }

    @Test
    public void contextLoads() throws Exception {
        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById("1001");

        //获取公钥
        String down_pub_key = downSpInfo.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        //获取私钥
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);

        CollpayInfo collpayInfo = new CollpayInfo();
        collpayInfo.setDown_sp_id("1001");
        collpayInfo.setDown_mch_id("101");

        collpayInfo.setOut_trade_no(System.currentTimeMillis() + "");
        collpayInfo.setBody("ffvvadfjo1");
        collpayInfo.setTotal_fee("8864");
        collpayInfo.setCard_type("CREDIT");
        collpayInfo.setCard_name("水1");
        collpayInfo.setCard_no("6222021001134258654");
        collpayInfo.setId_type("ID_CARD");
        collpayInfo.setId_no("410781199004016952");
        collpayInfo.setBank_mobile("12345678912");
        collpayInfo.setCvv2("123");
        collpayInfo.setCard_valid_date("0318");
        //collpayInfo.set("notify_url", "http://192.168.168.168");
        collpayInfo.setNonce_str("123456789");

        //公钥加密
        collpayInfo.setCard_name(RSAUtils.publicKeyEncrypt(collpayInfo.getCard_name(), rsaPublicKey));
        collpayInfo.setCard_no(RSAUtils.publicKeyEncrypt(collpayInfo.getCard_no(), rsaPublicKey));
        collpayInfo.setId_no(RSAUtils.publicKeyEncrypt(collpayInfo.getId_no(), rsaPublicKey));
        collpayInfo.setBank_mobile(RSAUtils.publicKeyEncrypt(collpayInfo.getBank_mobile(), rsaPublicKey));
        collpayInfo.setCvv2(RSAUtils.publicKeyEncrypt(collpayInfo.getCvv2(), rsaPublicKey));
        collpayInfo.setCard_valid_date(RSAUtils.publicKeyEncrypt(collpayInfo.getCard_valid_date(), rsaPublicKey));

        collpayInfoRespository.save(collpayInfo);

        Gson gson = new Gson();
        String s = gson.toJson(collpayInfo);

        //私钥签名
        collpayInfo.setSign(RSAUtils.sign(s, rsaPrivateKey));
    /*String sign = collpayInfo.getSign();
    System.out.println("签名信息"+sign);*/

        String collpayInfoToJson = gson.toJson(collpayInfo);

        System.out.println(collpayInfoToJson);


    }
}