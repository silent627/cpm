package com.wuzuhao.cpm.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 登录结果DTO
 */
@ApiModel(description = "登录响应结果")
@Data
public class LoginResultDTO {
    @ApiModelProperty(value = "JWT Token", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "用户名", example = "admin")
    private String username;

    @ApiModelProperty(value = "角色", example = "ADMIN")
    private String role;

    @ApiModelProperty(value = "真实姓名", example = "系统管理员")
    private String realName;
}

