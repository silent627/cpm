package com.wuzuhao.cpm.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录DTO
 */
@ApiModel(description = "登录请求参数")
@Data
public class LoginDTO {
    @ApiModelProperty(value = "用户名", required = true, example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "密码", required = true, example = "admin123")
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty(value = "验证码key", required = false)
    private String captchaKey;

    @ApiModelProperty(value = "验证码", required = false)
    private String captchaCode;
}

