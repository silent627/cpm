package com.wuzuhao.cpm.user.service;

import com.wuzuhao.cpm.user.dto.LoginDTO;
import com.wuzuhao.cpm.user.dto.LoginResultDTO;

/**
 * 认证服务接口
 */
public interface AuthService {
    /**
     * 登录
     */
    LoginResultDTO login(LoginDTO loginDTO);

    /**
     * 登出
     */
    void logout(String token);

    /**
     * 验证Token
     */
    boolean validateToken(String token);
}

