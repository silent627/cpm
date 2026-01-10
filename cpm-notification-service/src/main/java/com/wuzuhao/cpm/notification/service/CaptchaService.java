package com.wuzuhao.cpm.notification.service;

import java.util.Map;

/**
 * 验证码服务接口
 */
public interface CaptchaService {
    
    /**
     * 生成验证码
     * @return 包含验证码图片和key的Map
     */
    Map<String, Object> generateCaptcha();
    
    /**
     * 验证验证码
     * @param key 验证码key
     * @param code 用户输入的验证码
     * @return 验证是否通过
     */
    boolean validateCaptcha(String key, String code);
}
