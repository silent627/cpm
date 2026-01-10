package com.wuzuhao.cpm.notification.service;

/**
 * 邮件服务接口
 */
public interface EmailService {
    
    /**
     * 发送邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 是否发送成功
     */
    boolean sendEmail(String to, String subject, String content);
    
    /**
     * 发送HTML格式邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param htmlContent HTML邮件内容
     * @return 是否发送成功
     */
    boolean sendHtmlEmail(String to, String subject, String htmlContent);
    
    /**
     * 发送验证码邮件（HTML格式，包含自动验证按钮）
     * @param to 收件人邮箱
     * @param code 验证码
     * @param type 验证码类型（forget-忘记密码，change-修改密码）
     * @param baseUrl 前端基础URL，用于生成自动验证链接
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String to, String code, String type, String baseUrl);
}
