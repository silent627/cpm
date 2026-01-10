package com.wuzuhao.cpm.user.feign;

import com.wuzuhao.cpm.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 通知服务 Feign 客户端
 * 用于调用通知服务的邮件和验证码接口
 */
@FeignClient(name = "cpm-notification-service")
public interface NotificationServiceClient {

    /**
     * 发送邮箱验证码
     * @param email 邮箱地址
     * @param type 验证码类型
     * @return 发送结果
     */
    @PostMapping("/notification/email/code/send")
    Result<?> sendEmailCode(
            @RequestParam("email") String email,
            @RequestParam(value = "type", defaultValue = "forget") String type);

    /**
     * 验证邮箱验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @param type 验证码类型
     * @return 验证结果
     */
    @PostMapping("/notification/email/code/validate")
    Result<?> validateEmailCode(
            @RequestParam("email") String email,
            @RequestParam("code") String code,
            @RequestParam(value = "type", defaultValue = "forget") String type);

    /**
     * 仅验证邮箱验证码（不删除）
     * @param email 邮箱地址
     * @param code 验证码
     * @param type 验证码类型
     * @return 验证结果
     */
    @PostMapping("/notification/email/code/verify")
    Result<?> verifyEmailCodeOnly(
            @RequestParam("email") String email,
            @RequestParam("code") String code,
            @RequestParam(value = "type", defaultValue = "forget") String type);

    /**
     * 检查是否可以发送验证码
     * @param email 邮箱地址
     * @param type 验证码类型
     * @return 是否可以发送
     */
    @GetMapping("/notification/email/code/can-send")
    Result<Boolean> canSendCode(
            @RequestParam("email") String email,
            @RequestParam(value = "type", defaultValue = "forget") String type);

    /**
     * 获取剩余等待时间
     * @param email 邮箱地址
     * @param type 验证码类型
     * @return 剩余等待时间
     */
    @GetMapping("/notification/email/code/remaining")
    Result<Long> getRemainingSeconds(
            @RequestParam("email") String email,
            @RequestParam(value = "type", defaultValue = "forget") String type);

    /**
     * 生成图形验证码
     * @return 验证码图片和key
     */
    @GetMapping("/captcha/generate")
    Result<Map<String, Object>> generateCaptcha();

    /**
     * 验证图形验证码
     * @param key 验证码key
     * @param code 验证码
     * @return 验证结果
     */
    @PostMapping("/captcha/validate")
    Result<?> validateCaptcha(
            @RequestParam("key") String key,
            @RequestParam("code") String code);
}
