package com.wuzuhao.cpm.household.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.household.dto.HouseholdMemberDTO;
import com.wuzuhao.cpm.household.entity.HouseholdMember;
import com.wuzuhao.cpm.household.feign.ResidentServiceClient;
import com.wuzuhao.cpm.household.service.HouseholdMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 户籍成员控制器
 */
@Api(tags = "户籍成员管理")
@RestController
@RequestMapping("/household-member")
public class HouseholdMemberController {

    @Autowired
    private HouseholdMemberService householdMemberService;

    @Autowired
    private ResidentServiceClient residentServiceClient;

    /**
     * 添加成员到户籍
     */
    @ApiOperation(value = "添加成员到户籍", notes = "将居民添加到指定户籍")
    @PostMapping("/add")
    public Result<HouseholdMember> addMember(@RequestBody Map<String, Object> params) {
        Long householdId = Long.valueOf(params.get("householdId").toString());
        Long residentId = Long.valueOf(params.get("residentId").toString());
        String relationship = (String) params.get("relationship");
        HouseholdMember member = householdMemberService.addMember(householdId, residentId, relationship);
        return Result.success("添加成功", member);
    }

    /**
     * 从户籍中移除成员
     */
    @ApiOperation(value = "从户籍中移除成员", notes = "从指定户籍中移除居民")
    @DeleteMapping("/remove")
    public Result<?> removeMember(
            @ApiParam(value = "户籍ID", required = true) @RequestParam Long householdId,
            @ApiParam(value = "居民ID", required = true) @RequestParam Long residentId) {
        householdMemberService.removeMember(householdId, residentId);
        return Result.success("移除成功");
    }

    /**
     * 查询户籍的所有成员（包含居民详细信息）
     */
    @ApiOperation(value = "查询户籍的所有成员", notes = "分页查询指定户籍的所有成员，包含居民详细信息")
    @GetMapping("/list/{householdId}")
    public Result<Page<HouseholdMemberDTO>> getMembersByHouseholdId(
            @ApiParam(value = "户籍ID", required = true) @PathVariable Long householdId,
            @ApiParam(value = "当前页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        Page<HouseholdMember> page = new Page<>(current, size);
        LambdaQueryWrapper<HouseholdMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseholdMember::getHouseholdId, householdId);
        wrapper.orderByDesc(HouseholdMember::getCreateTime);
        Page<HouseholdMember> memberPage = householdMemberService.page(page, wrapper);

        // 转换为DTO，包含居民详细信息
        List<HouseholdMemberDTO> dtoList = new ArrayList<>();
        if (memberPage.getRecords() != null && !memberPage.getRecords().isEmpty()) {
            // 获取所有居民ID
            List<Long> residentIds = memberPage.getRecords().stream()
                    .map(HouseholdMember::getResidentId)
                    .collect(Collectors.toList());

            // 通过Feign批量查询居民信息
            List<Map<String, Object>> residents = new ArrayList<>();
            for (Long residentId : residentIds) {
                Result<Object> residentResult = residentServiceClient.getResidentById(residentId);
                if (residentResult != null && residentResult.getCode() == 200 && residentResult.getData() != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> residentData = (Map<String, Object>) residentResult.getData();
                    residents.add(residentData);
                }
            }
            
            Map<Long, Map<String, Object>> residentMap = residents.stream()
                    .filter(r -> r.get("id") != null)
                    .collect(Collectors.toMap(
                            r -> Long.valueOf(r.get("id").toString()),
                            r -> r
                    ));

            // 组装DTO
            for (HouseholdMember member : memberPage.getRecords()) {
                HouseholdMemberDTO dto = new HouseholdMemberDTO();
                BeanUtils.copyProperties(member, dto);
                
                Map<String, Object> resident = residentMap.get(member.getResidentId());
                if (resident != null) {
                    dto.setRealName(resident.get("realName") != null ? resident.get("realName").toString() : null);
                    dto.setIdCard(resident.get("idCard") != null ? resident.get("idCard").toString() : null);
                    if (resident.get("gender") != null) {
                        dto.setGender(Integer.valueOf(resident.get("gender").toString()));
                    }
                    if (resident.get("birthDate") != null) {
                        dto.setBirthDate(java.time.LocalDate.parse(resident.get("birthDate").toString()));
                    }
                    dto.setNationality(resident.get("nationality") != null ? resident.get("nationality").toString() : null);
                    dto.setCurrentAddress(resident.get("currentAddress") != null ? resident.get("currentAddress").toString() : null);
                    dto.setOccupation(resident.get("occupation") != null ? resident.get("occupation").toString() : null);
                    dto.setEducation(resident.get("education") != null ? resident.get("education").toString() : null);
                }
                dtoList.add(dto);
            }
        }

        // 构建返回结果
        Page<HouseholdMemberDTO> result = new Page<>(memberPage.getCurrent(), memberPage.getSize(), memberPage.getTotal());
        result.setRecords(dtoList);
        return Result.success(result);
    }
}

