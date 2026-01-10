package com.wuzuhao.cpm.notification.controller;

import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.notification.service.CaptchaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 验证码控制器
 */
@Api(tags = "验证码管理")
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 生成图形验证码
     */
    @ApiOperation(value = "生成验证码", notes = "生成图形验证码，返回验证码图片和key")
    @GetMapping("/generate")
    public Result<Map<String, Object>> generateCaptcha() {
        Map<String, Object> data = captchaService.generateCaptcha();
        return Result.success(data);
    }

    /**
     * 验证图形验证码
     */
    @ApiOperation(value = "验证验证码", notes = "验证图形验证码，验证成功后删除验证码")
    @PostMapping("/validate")
    public Result<?> validateCaptcha(
            @ApiParam(value = "验证码key", required = true) @RequestParam String key,
            @ApiParam(value = "验证码", required = true) @RequestParam String code) {
        boolean valid = captchaService.validateCaptcha(key, code);
        if (valid) {
            return Result.success("验证码验证成功");
        } else {
            return Result.error("验证码验证失败");
        }
    }
}
