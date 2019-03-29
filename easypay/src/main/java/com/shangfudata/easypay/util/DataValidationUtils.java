package com.shangfudata.easypay.util;

import com.shangfudata.easypay.constant.CardType;
import com.shangfudata.easypay.constant.IDType;
import com.shangfudata.easypay.entity.EasypayInfo;
import com.shangfudata.easypay.exception.*;

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
    public DataValidationUtils cardTypeValid(String cardType, String cvv2, String cardValidData) throws CardTypeError, CreditParamIsNullException {
        //isNullValid(cardType);
        switch (cardType) {
            // 若为贷记卡
            case CardType.CREDIT:
                if (cvv2.trim().equals("")) {
                    throw new CreditParamIsNullException();
                }
                if (cardValidData.trim().equals("")) {
                    throw new CreditParamIsNullException();
                }
                break;
            // 若为借记卡
            case CardType.DEBIT:
                // 卡类型错误
                break;
            default:
                throw new CardTypeError();
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
                if (!(RegexUtils.isIDCard18(idNo.trim()))) {
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

    // 卡号效验

    /**
     * 校验银行卡卡号
     *
     * @return
     */
    public DataValidationUtils bankCardValid(String cardNo) throws BankCardIDException {
        //isNullValid(cardNo);
        String cardId = cardNo;
        char bit = getBankCardCheckCode(cardId.substring(0, cardId.length() - 1));
        if (bit == 'N') {
            throw new BankCardIDException();
        }
        if (cardId.charAt(cardId.length() - 1) == bit) {
            throw new BankCardIDException();
        }
        return this.dataValidationUtils;
    }

    /**
     * 从不含校验位的银行卡卡号采用 Luhm 校验算法获得校验位
     *
     * @param nonCheckCodeCardId
     * @return
     */
    private static char getBankCardCheckCode(String nonCheckCodeCardId) {
        if (nonCheckCodeCardId == null || nonCheckCodeCardId.trim().length() == 0
                || !nonCheckCodeCardId.matches("\\d+")) {
            //如果传的不是数据返回N
            return 'N';
        }
        char[] chs = nonCheckCodeCardId.trim().toCharArray();
        int luhmSum = 0;
        for (int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
            int k = chs[i] - '0';
            if (j % 2 == 0) {
                k *= 2;
                k = k / 10 + k % 10;
            }
            luhmSum += k;
        }
        return (luhmSum % 10 == 0) ? '0' : (char) ((10 - luhmSum % 10) + '0');
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
    public void processEasyPayException(EasypayInfo easypayInfo, Map responseMap) {
        // 数据效验
        // 异常处理
        try {
            dataValidationUtils.totalFee(easypayInfo.getTotal_fee()).urlConnection(easypayInfo.getNotify_url()).bankCardValid(easypayInfo.getCard_no()).cardValid(easypayInfo.getId_type(),
                    easypayInfo.getId_type()).cardTypeValid(easypayInfo.getCard_type(), easypayInfo.getCvv2(),
                    easypayInfo.getCard_valid_date()).cardHolderNameValid(easypayInfo.getCard_name()).
                    mobileNumberValid(easypayInfo.getBank_mobile()).nonceStrValid(easypayInfo.getNonce_str());
        } catch (NonceStrLengthException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "随机字符串长度错误");
        } catch (NotMobileNumberError notMobileNumberError) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "手机号码验证错误");
        } catch (CardTypeError cardTypeError) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "银行卡类型错误");
        } catch (CreditParamIsNullException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "贷记卡参数为空");
        } catch (IDTypeLengthException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "证件号长度错误");
        } catch (IDTypeError idTypeError) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "证件类型错误");
        } catch (BankCardIDException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "银行卡号错误");
        } catch (MalformedURLException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("SUCCESS", "url 格式错误");
        } catch (IOException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("SUCCESS", "url 无法正常访问");
        }
    }
}
