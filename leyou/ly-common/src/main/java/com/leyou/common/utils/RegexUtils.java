package com.leyou.common.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 黑马程序员
 */
public class RegexUtils {
    /**
     * 是否符合手机格式
     * @param phone 要校验的手机号
     * @return true:符合，false：不符合
     */
    public static boolean isPhone(String phone){
        return matches(phone, RegexPatterns.PHONE_REGEX);
    }
    /**
     * 是否符合邮箱格式
     * @param email 要校验的邮箱
     * @return true:符合，false：不符合
     */
    public static boolean isEmail(String email){
        return matches(email, RegexPatterns.EMAIL_REGEX);
    }

    private static boolean matches(String str, String regex){
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches(regex);
    }

    /**
     * 判断验证码是否合法
     * @param code
     * @return true:合法，false：不合法
     */
    public static boolean isVerifyCode(String code){
        if(StringUtils.isBlank(code)){
            return false;
        }
        return code.matches(RegexPatterns.SMS_REGEX);
    }
}
