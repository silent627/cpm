package com.wuzuhao.cpm.household.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuzuhao.cpm.household.entity.HouseholdMember;

/**
 * 户籍成员服务接口
 */
public interface HouseholdMemberService extends IService<HouseholdMember> {
    /**
     * 添加成员到户籍
     */
    HouseholdMember addMember(Long householdId, Long residentId, String relationship);

    /**
     * 从户籍中移除成员
     */
    boolean removeMember(Long householdId, Long residentId);
}

