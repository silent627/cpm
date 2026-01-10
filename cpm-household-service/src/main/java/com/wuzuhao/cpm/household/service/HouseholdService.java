package com.wuzuhao.cpm.household.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuzuhao.cpm.household.entity.Household;

/**
 * 户籍服务接口
 */
public interface HouseholdService extends IService<Household> {
    /**
     * 根据户籍编号查询
     */
    Household getByHouseholdNo(String householdNo);

    /**
     * 创建户籍
     */
    Household createHousehold(Household household);

    /**
     * 更新户籍成员数量
     */
    void updateMemberCount(Long householdId);
}

