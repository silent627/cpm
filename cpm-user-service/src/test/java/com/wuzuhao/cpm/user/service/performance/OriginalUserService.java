package com.wuzuhao.cpm.user.service.performance;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzuhao.cpm.user.entity.User;
import com.wuzuhao.cpm.user.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 模拟原先代码的UserService实现
 * 直接查询数据库，不使用Redis缓存
 * 用于性能对比测试
 */
@Component
public class OriginalUserService extends ServiceImpl<UserMapper, User> {

    /**
     * 根据ID查询用户（不使用缓存）
     */
    public User getById(Long id) {
        if (id == null) {
            return null;
        }
        // 直接查询数据库，不使用缓存
        return super.getById(id);
    }

    /**
     * 根据用户名查询用户（不使用缓存）
     */
    public User getByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        // 直接查询数据库，不使用缓存
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.getOne(wrapper);
    }

    /**
     * 根据邮箱查询用户（不使用缓存）
     */
    public User getByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        // 直接查询数据库，不使用缓存
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return this.getOne(wrapper);
    }

    /**
     * 批量根据ID查询用户（不使用缓存）
     */
    public List<User> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        // 直接查询数据库，不使用缓存
        return super.listByIds(ids);
    }
}
