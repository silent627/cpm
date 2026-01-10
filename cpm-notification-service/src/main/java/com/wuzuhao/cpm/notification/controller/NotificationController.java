package com.wuzuhao.cpm.notification.controller;

import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.notification.service.EmailCodeService;
import com.wuzuhao.cpm.notification.service.EmailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知服务控制器
 */
@Api(tags = "通知服务管理")
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailCodeService emailCodeService;

    /**
     * 发送邮件
     */
    @ApiOperation(value = "发送邮件", notes = "发送普通邮件")
    @PostMapping("/email/send")
    public Result<?> sendEmail(
            @ApiParam(value = "收件人邮箱", required = true) @RequestParam String to,
            @ApiParam(value = "邮件主题", required = true) @RequestParam String subject,
            @ApiParam(value = "邮件内容", required = true) @RequestParam String content) {
        boolean success = emailService.sendEmail(to, subject, content);
        if (success) {
            return Result.success("邮件发送成功");
        } else {
            return Result.error("邮件发送失败");
        }
    }

    /**
     * 发送邮箱验证码
     */
    @ApiOperation(value = "发送邮箱验证码", notes = "发送邮箱验证码，60秒内只能发送一次")
    @PostMapping("/email/code/send")
    public Result<?> sendEmailCode(
            @ApiParam(value = "邮箱地址", required = true) @RequestParam String email,
            @ApiParam(value = "验证码类型", example = "forget") @RequestParam(defaultValue = "forget") String type) {
        boolean success = emailCodeService.sendEmailCode(email, type);
        if (success) {
            return Result.success("验证码发送成功");
        } else {
            long remaining = emailCodeService.getRemainingSeconds(email, type);
            if (remaining > 0) {
                return Result.error("发送过于频繁，请等待 " + remaining + " 秒后重试");
            }
            return Result.error("验证码发送失败");
        }
    }

    /**
     * 验证邮箱验证码
     */
    @ApiOperation(value = "验证邮箱验证码", notes = "验证邮箱验证码，验证成功后删除验证码")
    @PostMapping("/email/code/validate")
    public Result<?> validateEmailCode(
            @ApiParam(value = "邮箱地址", required = true) @RequestParam String email,
            @ApiParam(value = "验证码", required = true) @RequestParam String code,
            @ApiParam(value = "验证码类型", example = "forget") @RequestParam(defaultValue = "forget") String type) {
        boolean valid = emailCodeService.validateEmailCode(email, code, type);
        if (valid) {
            return Result.success("验证码验证成功");
        } else {
            return Result.error("验证码验证失败");
        }
    }

    /**
     * 仅验证邮箱验证码（不删除）
     */
    @ApiOperation(value = "仅验证邮箱验证码", notes = "仅验证邮箱验证码，不删除验证码（用于中间步骤验证）")
    @PostMapping("/email/code/verify")
    public Result<?> verifyEmailCodeOnly(
            @ApiParam(value = "邮箱地址", required = true) @RequestParam String email,
            @ApiParam(value = "验证码", required = true) @RequestParam String code,
            @ApiParam(value = "验证码类型", example = "forget") @RequestParam(defaultValue = "forget") String type) {
        boolean valid = emailCodeService.verifyEmailCodeOnly(email, code, type);
        if (valid) {
            return Result.success("验证码验证成功");
        } else {
            return Result.error("验证码验证失败");
        }
    }

    /**
     * 检查是否可以发送验证码
     */
    @ApiOperation(value = "检查是否可以发送验证码", notes = "检查是否可以发送验证码（60秒间隔限制）")
    @GetMapping("/email/code/can-send")
    public Result<Boolean> canSendCode(
            @ApiParam(value = "邮箱地址", required = true) @RequestParam String email,
            @ApiParam(value = "验证码类型", example = "forget") @RequestParam(defaultValue = "forget") String type) {
        boolean canSend = emailCodeService.canSendCode(email, type);
        return Result.success(canSend ? "可以发送" : "需要等待", canSend);
    }

    /**
     * 获取剩余等待时间
     */
    @ApiOperation(value = "获取剩余等待时间", notes = "获取剩余等待时间（秒）")
    @GetMapping("/email/code/remaining")
    public Result<Long> getRemainingSeconds(
            @ApiParam(value = "邮箱地址", required = true) @RequestParam String email,
            @ApiParam(value = "验证码类型", example = "forget") @RequestParam(defaultValue = "forget") String type) {
        long remaining = emailCodeService.getRemainingSeconds(email, type);
        return Result.success("剩余等待时间", remaining);
    }

}
