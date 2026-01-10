package com.wuzuhao.cpm.household.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzuhao.cpm.household.entity.Household;
import com.wuzuhao.cpm.household.entity.HouseholdMember;
import com.wuzuhao.cpm.household.mapper.HouseholdMapper;
import com.wuzuhao.cpm.household.mapper.HouseholdMemberMapper;
import com.wuzuhao.cpm.household.service.HouseholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 户籍服务实现类
 */
@Service
public class HouseholdServiceImpl extends ServiceImpl<HouseholdMapper, Household> implements HouseholdService {

    @Autowired
    private HouseholdMemberMapper householdMemberMapper;

    @Override
    public Household getByHouseholdNo(String householdNo) {
        LambdaQueryWrapper<Household> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Household::getHouseholdNo, householdNo);
        return this.getOne(wrapper);
    }

    @Override
    public Household createHousehold(Household household) {
        household.setStatus(1);
        household.setMemberCount(0);
        // createTime 和 updateTime 由 MyBatis-Plus 自动填充
        this.save(household);
        return household;
    }

    @Override
    public void updateMemberCount(Long householdId) {
        LambdaQueryWrapper<HouseholdMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseholdMember::getHouseholdId, householdId);
        long count = householdMemberMapper.selectCount(wrapper);

        Household household = this.getById(householdId);
        if (household != null) {
            household.setMemberCount((int) count);
            // updateTime 由 MyBatis-Plus 自动填充
            this.updateById(household);
        }
    }
}

