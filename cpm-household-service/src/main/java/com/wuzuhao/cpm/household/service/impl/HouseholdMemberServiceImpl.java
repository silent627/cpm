package com.wuzuhao.cpm.household.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzuhao.cpm.household.entity.HouseholdMember;
import com.wuzuhao.cpm.household.mapper.HouseholdMemberMapper;
import com.wuzuhao.cpm.household.service.HouseholdMemberService;
import com.wuzuhao.cpm.household.service.HouseholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 户籍成员服务实现类
 */
@Service
public class HouseholdMemberServiceImpl extends ServiceImpl<HouseholdMemberMapper, HouseholdMember> implements HouseholdMemberService {

    @Autowired
    private HouseholdService householdService;

    @Override
    public HouseholdMember addMember(Long householdId, Long residentId, String relationship) {
        // 检查是否已存在
        LambdaQueryWrapper<HouseholdMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseholdMember::getHouseholdId, householdId)
                .eq(HouseholdMember::getResidentId, residentId);
        HouseholdMember exist = this.getOne(wrapper);
        if (exist != null) {
            throw new RuntimeException("该成员已在此户籍中");
        }

        HouseholdMember member = new HouseholdMember();
        member.setHouseholdId(householdId);
        member.setResidentId(residentId);
        member.setRelationship(relationship);
        // createTime 和 updateTime 由 MyBatis-Plus 自动填充
        this.save(member);

        // 更新户籍成员数量
        householdService.updateMemberCount(householdId);

        return member;
    }

    @Override
    public boolean removeMember(Long householdId, Long residentId) {
        LambdaQueryWrapper<HouseholdMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseholdMember::getHouseholdId, householdId)
                .eq(HouseholdMember::getResidentId, residentId);
        boolean result = this.remove(wrapper);

        // 更新户籍成员数量
        if (result) {
            householdService.updateMemberCount(householdId);
        }

        return result;
    }
}

