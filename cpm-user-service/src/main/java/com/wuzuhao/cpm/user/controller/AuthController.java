package com.wuzuhao.cpm.user.controller;

import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.user.dto.LoginDTO;
import com.wuzuhao.cpm.user.dto.LoginResultDTO;
import com.wuzuhao.cpm.user.service.AuthService;
import com.wuzuhao.cpm.user.feign.NotificationServiceClient;
import com.wuzuhao.cpm.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * 认证控制器
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    @Lazy
    private NotificationServiceClient notificationServiceClient;

    @Autowired
    private UserService userService;

    /**
     * 发送忘记密码验证码
     */
    @ApiOperation(value = "发送忘记密码验证码", notes = "向指定邮箱发送验证码，用于重置密码")
    @PostMapping("/forget-password/send-code")
    public Result<?> sendForgetPasswordCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        if (email == null || email.trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        
        // 检查用户是否存在
        com.wuzuhao.cpm.user.entity.User user = userService.getByEmail(email);
        if (user == null) {
            return Result.error("该邮箱未注册");
        }

        // 检查是否可以发送（60秒间隔）
        Result<Boolean> canSendResult = notificationServiceClient.canSendCode(email, "forget");
        if (canSendResult.getCode() != 200 || !canSendResult.getData()) {
            Result<Long> remainingResult = notificationServiceClient.getRemainingSeconds(email, "forget");
            long remaining = remainingResult.getCode() == 200 ? remainingResult.getData() : 60;
            return Result.error("发送过于频繁，请" + remaining + "秒后再试");
        }

        Result<?> sendResult = notificationServiceClient.sendEmailCode(email, "forget");
        boolean success = sendResult.getCode() == 200;
        if (success) {
            return Result.success("验证码已发送，请查收邮件");
        } else {
            return Result.error("验证码发送失败，可能是邮件服务未配置。请检查服务器日志或联系管理员。验证码已生成，请查看服务器控制台日志。");
        }
    }

    /**
     * 验证忘记密码验证码
     */
    @ApiOperation(value = "验证忘记密码验证码", notes = "验证邮箱验证码是否正确")
    @PostMapping("/forget-password/verify-code")
    public Result<?> verifyForgetPasswordCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");

        if (email == null || email.trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            return Result.error("验证码不能为空");
        }

        // 验证邮箱验证码（仅验证，不删除，因为后续重置密码时还需要使用）
        Result<?> verifyResult = notificationServiceClient.verifyEmailCodeOnly(email, code, "forget");
        if (verifyResult.getCode() != 200) {
            return Result.error("验证码错误或已过期");
        }

        return Result.success("验证码正确");
    }

    /**
     * 重置密码（忘记密码）
     */
    @ApiOperation(value = "重置密码", notes = "通过邮箱验证码重置密码")
    @PostMapping("/forget-password/reset")
    public Result<?> resetPassword(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");

        if (email == null || email.trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            return Result.error("验证码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error("新密码不能为空");
        }

        // 验证邮箱验证码
        Result<?> validateResult = notificationServiceClient.validateEmailCode(email, code, "forget");
        if (validateResult.getCode() != 200) {
            return Result.error("验证码错误或已过期");
        }

        // 查找用户
        com.wuzuhao.cpm.user.entity.User user = userService.getByEmail(email);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 重置密码（使用changePassword方法，但不需要旧密码）
        try {
            userService.changePasswordByEmail(user.getId(), newPassword);
            return Result.success("密码重置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 登录
     */
    @ApiOperation(value = "用户登录", notes = "使用用户名和密码登录，返回JWT Token")
    @PostMapping("/login")
    public Result<LoginResultDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResultDTO result = authService.login(loginDTO);
        return Result.success("登录成功", result);
    }

    /**
     * 登出
     */
    @ApiOperation(value = "用户登出", notes = "退出登录，清除Token。需要在请求头中携带Authorization: Bearer {token}")
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            authService.logout(token);
        }
        return Result.success("登出成功");
    }
}

