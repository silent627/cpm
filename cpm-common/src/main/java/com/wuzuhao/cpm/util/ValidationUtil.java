package com.wuzuhao.cpm.util;

import java.util.regex.Pattern;

/**
 * 验证工具类
 */
public class ValidationUtil {

    // 身份证号正则：18位数字
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{18}$");
    
    // 手机号正则：11位数字，1开头
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // 密码强度正则：至少8位，包含大小写字母、数字和特殊字符
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    
    // 中等密码强度：至少8位，包含字母和数字
    private static final Pattern MEDIUM_PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$");

    /**
     * 验证身份证号格式（18位数字）
     */
    public static boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return false;
        }
        return ID_CARD_PATTERN.matcher(idCard.trim()).matches();
    }

    /**
     * 验证手机号格式（11位数字，1开头）
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * 验证密码长度（只检查长度，不强制其他规则）
     * @param password 密码
     * @return true-长度符合要求（>=8位）, false-长度不足
     */
    public static boolean validatePasswordLength(String password) {
        return password != null && password.length() >= 8;
    }

    /**
     * 验证密码强度（仅用于提示，不强制）
     * @param password 密码
     * @return 0-弱密码, 1-中等密码, 2-强密码
     */
    public static int validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return 0; // 弱密码
        }
        if (STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            return 2; // 强密码
        }
        if (MEDIUM_PASSWORD_PATTERN.matcher(password).matches()) {
            return 1; // 中等密码
        }
        return 0; // 弱密码
    }

    /**
     * 获取密码强度描述（仅用于提示）
     */
    public static String getPasswordStrengthDescription(String password) {
        if (password == null || password.length() < 8) {
            return "密码长度至少8位";
        }
        int strength = validatePasswordStrength(password);
        switch (strength) {
            case 2:
                return "强密码";
            case 1:
                return "中等密码";
            default:
                return "弱密码（建议包含大小写字母、数字和特殊字符）";
        }
    }
}

