package com.wuzuhao.cpm.resident.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wuzuhao.cpm.resident.entity.Resident;

/**
 * 居民服务接口
 */
public interface ResidentService extends IService<Resident> {
    /**
     * 根据用户ID查询居民
     */
    Resident getByUserId(Long userId);

    /**
     * 根据身份证号查询居民
     */
    Resident getByIdCard(String idCard);

    /**
     * 创建居民
     */
    Resident createResident(Long userId, Resident resident);

    /**
     * 使用 MyBatis-Plus 进行模糊查询（仅用于性能测试）
     * @param keyword 搜索关键词
     * @param current 当前页码（从1开始）
     * @param size 每页数量
     * @return 分页结果
     */
    Page<Resident> searchByMyBatisPlus(String keyword, Integer current, Integer size);
}

