package com.wuzuhao.cpm.resident.service;

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
}

