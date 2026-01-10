package com.wuzuhao.cpm.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzuhao.cpm.user.entity.Admin;
import com.wuzuhao.cpm.user.mapper.AdminMapper;
import com.wuzuhao.cpm.user.service.AdminService;
import org.springframework.stereotype.Service;

/**
 * 管理员服务实现类
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public Admin getByUserId(Long userId) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUserId, userId);
        return this.getOne(wrapper);
    }

    @Override
    public Admin createAdmin(Long userId, Admin admin) {
        admin.setUserId(userId);
        // createTime 和 updateTime 由 MyBatis-Plus 自动填充
        this.save(admin);
        return admin;
    }
}

