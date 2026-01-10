package com.wuzuhao.cpm.household.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wuzuhao.cpm.household.dto.excel.HouseholdExcelDTO;
import com.wuzuhao.cpm.household.entity.Household;
import com.wuzuhao.cpm.household.service.impl.HouseholdServiceImpl;
import com.wuzuhao.cpm.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Excel导出服务（户籍服务专用）
 */
@Service
public class ExcelExportService {

    @Autowired
    private HouseholdServiceImpl householdService;

    /**
     * 导出户籍列表
     */
    public List<HouseholdExcelDTO> exportHouseholds(String householdNo, String headName, String address, Integer status) {
        LambdaQueryWrapper<Household> wrapper = new LambdaQueryWrapper<>();
        if (householdNo != null && !householdNo.isEmpty()) {
            wrapper.like(Household::getHouseholdNo, householdNo);
        }
        if (headName != null && !headName.isEmpty()) {
            wrapper.like(Household::getHeadName, headName);
        }
        if (address != null && !address.isEmpty()) {
            wrapper.like(Household::getAddress, address);
        }
        if (status != null) {
            wrapper.eq(Household::getStatus, status);
        }
        wrapper.orderByDesc(Household::getCreateTime);
        
        List<Household> households = householdService.list(wrapper);
        
        // 转换为Excel DTO
        return households.stream().map(household -> {
            HouseholdExcelDTO dto = new HouseholdExcelDTO();
            dto.setId(household.getId());
            dto.setHouseholdNo(household.getHouseholdNo());
            dto.setHeadName(household.getHeadName());
            dto.setHeadIdCard(household.getHeadIdCard());
            dto.setAddress(household.getAddress());
            dto.setHouseholdType(household.getHouseholdType() != null && household.getHouseholdType() == 1 ? "家庭户" : "集体户");
            dto.setMemberCount(household.getMemberCount());
            dto.setContactPhone(household.getContactPhone());
            dto.setStatus(household.getStatus() != null && household.getStatus() == 1 ? "正常" : "迁出");
            dto.setMoveInDate(ExcelUtil.formatDateTime(household.getMoveInDate()));
            dto.setMoveInReason(household.getMoveInReason());
            dto.setCreateTime(ExcelUtil.formatDateTime(household.getCreateTime()));
            return dto;
        }).collect(Collectors.toList());
    }
}

