package com.wuzuhao.cpm.common;

/**
 * 响应状态码枚举
 */
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "参数错误"),
    LOGIN_ERROR(401, "用户名或密码错误"),
    TOKEN_EXPIRED(401, "Token已过期"),
    USER_NOT_FOUND(404, "用户不存在"),
    USER_DISABLED(403, "用户已被禁用");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

