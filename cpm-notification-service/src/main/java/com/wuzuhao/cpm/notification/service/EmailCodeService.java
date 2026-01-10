package com.wuzuhao.cpm.notification.service;

/**
 * 邮箱验证码服务接口
 */
public interface EmailCodeService {
    
    /**
     * 发送邮箱验证码
     * @param email 邮箱地址
     * @param type 验证码类型：forget-忘记密码，change-修改密码
     * @return 是否发送成功
     */
    boolean sendEmailCode(String email, String type);
    
    /**
     * 验证邮箱验证码（验证后删除，一次性使用）
     * @param email 邮箱地址
     * @param code 验证码
     * @param type 验证码类型
     * @return 是否验证通过
     */
    boolean validateEmailCode(String email, String code, String type);
    
    /**
     * 仅验证邮箱验证码（不删除，用于中间步骤验证）
     * @param email 邮箱地址
     * @param code 验证码
     * @param type 验证码类型
     * @return 是否验证通过
     */
    boolean verifyEmailCodeOnly(String email, String code, String type);
    
    /**
     * 检查是否可以发送验证码（60秒间隔限制）
     * @param email 邮箱地址
     * @param type 验证码类型
     * @return 是否可以发送（true-可以发送，false-需要等待）
     */
    boolean canSendCode(String email, String type);
    
    /**
     * 获取剩余等待时间（秒）
     * @param email 邮箱地址
     * @param type 验证码类型
     * @return 剩余等待时间，0表示可以发送
     */
    long getRemainingSeconds(String email, String type);
}
