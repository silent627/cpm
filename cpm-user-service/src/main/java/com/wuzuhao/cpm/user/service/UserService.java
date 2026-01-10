package com.wuzuhao.cpm.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuzuhao.cpm.user.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    User getByEmail(String email);

    /**
     * 注册用户
     */
    User register(User user);

    /**
     * 更新用户信息
     */
    boolean updateUser(User user);

    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 通过邮箱重置密码（忘记密码时使用）
     */
    boolean changePasswordByEmail(Long userId, String newPassword);
}

