package com.wuzuhao.cpm.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuzuhao.cpm.user.entity.Admin;

/**
 * 管理员服务接口
 */
public interface AdminService extends IService<Admin> {
    /**
     * 根据用户ID查询管理员
     */
    Admin getByUserId(Long userId);

    /**
     * 创建管理员
     */
    Admin createAdmin(Long userId, Admin admin);
}

