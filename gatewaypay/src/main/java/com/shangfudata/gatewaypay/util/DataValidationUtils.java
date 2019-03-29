package com.shangfudata.gatewaypay.util;

import com.shangfudata.gatewaypay.constant.CardType;
import com.shangfudata.gatewaypay.constant.IDType;
import com.shangfudata.gatewaypay.constant.SettleAccType;
import com.shangfudata.gatewaypay.entity.GatewaypayInfo;
import com.shangfudata.gatewaypay.exception.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by tinlly to 2019/3/28
 * Package for com.shangfudata.collpay.util
 */
public class DataValidationUtils {

    static DataValidationUtils dataValidationUtils;

    public static DataValidationUtils builder() {
        return dataValidationUtils = new DataValidationUtils();
    }

    /**
     * 判断是否为空
     */
    public String isNullValid(Map<String, String> map) {
        for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
            try {
                isNullValid(stringStringEntry.getValue());
            } catch (NullPointerException e) {
                return stringStringEntry.getKey() + "不能为空";
            }
        }
        return "";
    }


    /**
     * 为空判断
     */
    public DataValidationUtils isNullValid(String string) throws NullPointerException {
        // string 判断为空
        if ("".equals(string) || null == string) {
            throw new NullPointerException();
        }
        return this.dataValidationUtils;
    }

    /**
     * 卡类型效验
     */
    public DataValidationUtils cardTypeValid(String cardType) throws CardTypeError, CreditParamIsNullException {
        //isNullValid(cardType);
        switch (cardType) {
            // 若为贷记卡
            case CardType.CREDIT:
                break;
            // 若为借记卡
            case CardType.DEBIT:
                break;
            default:
                throw new CardTypeError();
                // 卡类型错误
        }
        return this.dataValidationUtils;
    }

    /**
     * 证件类型效验
     */
    public DataValidationUtils cardValid(String idType, String idNo) throws IDTypeLengthException, IDTypeError {
        //isNullValid(idType);
        // id card 验证
        switch (idType) {
            case IDType.ID_CARD:
                // 证件验证
                if (!(RegexUtils.isIDCard18(idNo))) {
                    System.out.println("身份证验证错误");
                    // 不为银行卡号
                    throw new IDTypeLengthException();
                }
                break;
            default:
                // 证件类型错误
                throw new IDTypeError();
        }
        return this.dataValidationUtils;
    }

    /**
     * 持卡人姓名效验
     */
    public DataValidationUtils cardHolderNameValid(String cardHolderName) throws CardNameException {
        //isNullValid(cardHolderName);
        // id card 验证
        // 判断 持卡人 名称不为空
        // 判断 持卡人 名称不包含
        String trim = cardHolderName.trim();

        // 持卡人姓名长度小于 2 并且不为中文时抛出异常
        if (trim.length() < 2 || !RegexUtils.isZh(trim)) {
            // 持卡人姓名错误
            throw new CardNameException();
        }
        return this.dataValidationUtils;
    }

    /**
     * 校验银行卡卡号
     *
     * @return
     */
    public DataValidationUtils bankCardValid(String cardNo) throws BankCardIDException {
        if (!(RegexUtils.isBankCardNo(cardNo))) {
            throw new BankCardIDException();
        }
        //isNullValid(cardNo);
        //String cardId = cardNo;
        //char bit = getBankCardCheckCode(cardId.substring(0, cardId.length() - 1));
        //if (bit == 'N') {
        //    throw new BankCardIDException();
        //}
        //if (cardId.charAt(cardId.length() - 1) == bit) {
        //    throw new BankCardIDException();
        //}
        return this.dataValidationUtils;
    }

    /**
     * 手机号效验
     *
     * @throws NotMobileNumberError
     * @throws NullPointerException
     */
    public DataValidationUtils mobileNumberValid(String bankMobile) throws NotMobileNumberError {
        //isNullValid(bankMobile);
        if (!(RegexUtils.isMobileExact(bankMobile))) {
            throw new NotMobileNumberError();
        }
        return this.dataValidationUtils;
    }

    /**
     * 随机字符串效验
     *
     * @throws NonceStrLengthException
     * @throws NullPointerException
     */
    public DataValidationUtils nonceStrValid(String nonceStr) throws NonceStrLengthException {
        //isNullValid(nonceStr);
        if (!(nonceStr.length() == 32)) {
            throw new NonceStrLengthException();
        }
        return this.dataValidationUtils;
    }

    /**
     * 账户类型效验
     *
     * @param settleAccType
     * @return
     * @throws Exception
     */
    public DataValidationUtils settleAccTypeVaild(String settleAccType, String idType) throws CorporateIdCardNullException {
        switch (settleAccType) {
            case SettleAccType.PERSONNEL:
                break;
            case SettleAccType.CORPORATE:
                // 身份证验证错误
                if (!(RegexUtils.isIDCard18(idType))) {
                    throw new CorporateIdCardNullException();
                }
                break;
            default:
                // 卡类型错误
                throw new CardTypeError();
        }
        return this.dataValidationUtils;
    }

    /**
     * 金额效验
     *
     * @param totalFee
     */
    public DataValidationUtils totalFee(String totalFee) throws MoneyNotValidException {
        if (!(RegexUtils.isMoney(totalFee))) {
            throw new MoneyNotValidException();
        }
        return this.dataValidationUtils;
    }

    /**
     * url connection 效验
     *
     * @param url
     * @throws MalformedURLException
     * @throws IOException
     */
    public DataValidationUtils urlConnection(String url) throws MalformedURLException, IOException {
        URL urlProxy = new URL(url);
        urlProxy.openConnection();
        return this.dataValidationUtils;
    }

    /**
     * CollPay 下游请求参数异常处理方法
     */
    public void processDistillPayException(GatewaypayInfo gatewaypayInfo, Map responseMap) {
        // 数据效验
        // 异常处理
        try {
            dataValidationUtils.builder().totalFee(gatewaypayInfo.getTotal_fee())
                    .urlConnection(gatewaypayInfo.getNotify_url())
                    .urlConnection(gatewaypayInfo.getCall_back_url()).
                    cardTypeValid(gatewaypayInfo.getCard_type()).nonceStrValid(gatewaypayInfo.getNonce_str());
        } catch (MoneyNotValidException e) {
            responseMap.put("status","FAIL");
            responseMap.put("SUCCESS","金额格式错误");
        } catch (MalformedURLException e) {
            responseMap.put("status","FAIL");
            responseMap.put("SUCCESS","url 格式错误");
        } catch (IOException e) {
            responseMap.put("status","FAIL");
            responseMap.put("SUCCESS","url 无法正常访问");
        } catch (CardTypeError e) {
            responseMap.put("status","FAIL");
            responseMap.put("SUCCESS","卡类型错误");
        } catch (NonceStrLengthException e) {
            responseMap.put("status","FAIL");
            responseMap.put("SUCCESS","随机字符串长度错误");
        }
    }

}
