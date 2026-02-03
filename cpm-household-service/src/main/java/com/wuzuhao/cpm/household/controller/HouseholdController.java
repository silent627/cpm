package com.wuzuhao.cpm.household.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.household.dto.excel.HouseholdExcelDTO;
import com.wuzuhao.cpm.household.entity.Household;
import com.wuzuhao.cpm.household.feign.SearchServiceClient;
import com.wuzuhao.cpm.household.service.ExcelExportService;
import com.wuzuhao.cpm.household.service.HouseholdService;
import com.wuzuhao.cpm.util.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 户籍控制器
 */
@Slf4j
@Api(tags = "户籍管理")
@RestController
@RequestMapping("/household")
public class HouseholdController {

    @Autowired
    private HouseholdService householdService;

    @Autowired
    private SearchServiceClient searchServiceClient;

    @Autowired
    private ExcelExportService excelExportService;

    /**
     * 创建户籍
     */
    @ApiOperation(value = "创建户籍", notes = "创建新的户籍信息")
    @PostMapping("/create")
    public Result<Household> createHousehold(@RequestBody Household household) {
        // 生成户籍编号（实际应该使用更复杂的规则）
        if (household.getHouseholdNo() == null || household.getHouseholdNo().isEmpty()) {
            household.setHouseholdNo("HH" + System.currentTimeMillis());
        }
        household = householdService.createHousehold(household);
        return Result.success("创建成功", household);
    }

    /**
     * 更新户籍信息
     */
    @ApiOperation(value = "更新户籍信息", notes = "更新指定户籍的信息")
    @PutMapping("/update/{id}")
    public Result<?> updateHousehold(@ApiParam(value = "户籍ID", required = true) @PathVariable Long id, @RequestBody Household household) {
        household.setId(id);
        // 清除时间字段，让 MyBatis-Plus 自动填充
        household.setCreateTime(null);
        household.setUpdateTime(null);
        householdService.updateById(household);
        return Result.success("更新成功");
    }

    /**
     * 分页查询户籍列表（使用Elasticsearch全文检索）
     */
    @ApiOperation(value = "分页查询户籍列表", notes = "使用Elasticsearch全文检索查询户籍信息")
    @GetMapping("/list")
    public Result<Page<Household>> getHouseholdList(
            @ApiParam(value = "当前页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "户籍编号（模糊查询）") @RequestParam(required = false) String householdNo,
            @ApiParam(value = "户主姓名（模糊查询）") @RequestParam(required = false) String headName,
            @ApiParam(value = "户籍地址（模糊查询）") @RequestParam(required = false) String address,
            @ApiParam(value = "状态：0-迁出, 1-正常") @RequestParam(required = false) Integer status) {
        try {
            // 合并查询参数为keyword
            StringBuilder keywordBuilder = new StringBuilder();
            if (householdNo != null && !householdNo.trim().isEmpty()) {
                keywordBuilder.append(householdNo.trim());
            }
            if (headName != null && !headName.trim().isEmpty()) {
                if (keywordBuilder.length() > 0) {
                    keywordBuilder.append(" ");
                }
                keywordBuilder.append(headName.trim());
            }
            if (address != null && !address.trim().isEmpty()) {
                if (keywordBuilder.length() > 0) {
                    keywordBuilder.append(" ");
                }
                keywordBuilder.append(address.trim());
            }
            
            String keyword = keywordBuilder.length() > 0 ? keywordBuilder.toString() : "*";
            
            // 调用搜索服务（page参数从1开始，Elasticsearch从0开始，需要减1）
            Result<Map<String, Object>> searchResult = searchServiceClient.searchHousehold(keyword, current - 1, size);
            
            if (searchResult == null || searchResult.getCode() != 200 || searchResult.getData() == null) {
                // 如果搜索服务失败，返回空结果
                Page<Household> page = new Page<>(current, size);
                page.setTotal(0);
                return Result.success(page);
            }
            
            Map<String, Object> data = searchResult.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> hits = (List<Map<String, Object>>) data.get("hits");
            Long total = ((Number) data.get("total")).longValue();
            
            // 转换为Household实体列表
            List<Household> households = new ArrayList<>();
            if (hits != null) {
                for (Map<String, Object> hit : hits) {
                    try {
                        Household household = convertMapToHousehold(hit);
                        // 如果指定了status参数，进行过滤
                        if (status != null && household.getStatus() != null && !household.getStatus().equals(status)) {
                            continue;
                        }
                        households.add(household);
                    } catch (Exception e) {
                        // 忽略转换失败的数据
                        continue;
                    }
                }
            }
            
            // 如果进行了status过滤，需要重新计算总数（简单处理：使用过滤后的数量）
            if (status != null) {
                total = (long) households.size();
            }
            
            // 构建Page对象
            Page<Household> page = new Page<>(current, size);
            page.setTotal(total);
            page.setRecords(households);
            return Result.success(page);
            
        } catch (Exception e) {
            log.error("搜索户籍信息失败", e);
            // 如果搜索服务异常，返回空结果
            Page<Household> page = new Page<>(current, size);
            page.setTotal(0);
            return Result.success(page);
        }
    }
    
    /**
     * 将Map转换为Household实体
     */
    private Household convertMapToHousehold(Map<String, Object> map) {
        Household household = new Household();
        if (map.get("id") != null) {
            household.setId(Long.valueOf(map.get("id").toString()));
        }
        if (map.get("headId") != null) {
            household.setHeadId(Long.valueOf(map.get("headId").toString()));
        }
        household.setHeadName((String) map.get("headName"));
        household.setHeadIdCard((String) map.get("headIdCard"));
        household.setHouseholdNo((String) map.get("householdNo"));
        household.setAddress((String) map.get("address"));
        if (map.get("householdType") != null) {
            household.setHouseholdType(Integer.valueOf(map.get("householdType").toString()));
        }
        if (map.get("memberCount") != null) {
            household.setMemberCount(Integer.valueOf(map.get("memberCount").toString()));
        }
        household.setContactPhone((String) map.get("contactPhone"));
        
        // 使用 DateTimeFormatter 指定日期格式 "yyyy-MM-dd HH:mm:ss"
        java.time.format.DateTimeFormatter dateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        if (map.get("moveInDate") != null) {
            try {
                String moveInDateStr = map.get("moveInDate").toString();
                household.setMoveInDate(java.time.LocalDateTime.parse(moveInDateStr, dateTimeFormatter));
                log.debug("成功解析户籍 moveInDate: {}", moveInDateStr);
            } catch (Exception e) {
                log.warn("解析户籍 moveInDate 失败: {}, 错误: {}", map.get("moveInDate"), e.getMessage());
                // 忽略日期解析错误，继续处理其他字段
            }
        }
        if (map.get("moveOutDate") != null) {
            try {
                String moveOutDateStr = map.get("moveOutDate").toString();
                household.setMoveOutDate(java.time.LocalDateTime.parse(moveOutDateStr, dateTimeFormatter));
                log.debug("成功解析户籍 moveOutDate: {}", moveOutDateStr);
            } catch (Exception e) {
                log.warn("解析户籍 moveOutDate 失败: {}, 错误: {}", map.get("moveOutDate"), e.getMessage());
                // 忽略日期解析错误，继续处理其他字段
            }
        }
        household.setMoveInReason((String) map.get("moveInReason"));
        household.setMoveOutReason((String) map.get("moveOutReason"));
        if (map.get("status") != null) {
            household.setStatus(Integer.valueOf(map.get("status").toString()));
        }
        household.setRemark((String) map.get("remark"));
        if (map.get("createTime") != null) {
            try {
                String createTimeStr = map.get("createTime").toString();
                household.setCreateTime(java.time.LocalDateTime.parse(createTimeStr, dateTimeFormatter));
                log.debug("成功解析户籍 createTime: {}", createTimeStr);
            } catch (Exception e) {
                log.warn("解析户籍 createTime 失败: {}, 错误: {}", map.get("createTime"), e.getMessage());
                // 忽略日期解析错误，继续处理其他字段
            }
        }
        if (map.get("updateTime") != null) {
            try {
                String updateTimeStr = map.get("updateTime").toString();
                household.setUpdateTime(java.time.LocalDateTime.parse(updateTimeStr, dateTimeFormatter));
                log.debug("成功解析户籍 updateTime: {}", updateTimeStr);
            } catch (Exception e) {
                log.warn("解析户籍 updateTime 失败: {}, 错误: {}", map.get("updateTime"), e.getMessage());
                // 忽略日期解析错误，继续处理其他字段
            }
        }
        return household;
    }

    /**
     * 根据ID获取户籍
     */
    @ApiOperation(value = "根据ID获取户籍", notes = "根据户籍ID获取详细信息")
    @GetMapping("/{id}")
    public Result<Household> getHouseholdById(@ApiParam(value = "户籍ID", required = true) @PathVariable Long id) {
        Household household = householdService.getById(id);
        return Result.success(household);
    }

    /**
     * 获取所有户籍列表（用于统计服务）
     */
    @ApiOperation(value = "获取所有户籍列表", notes = "获取所有户籍信息，用于统计服务")
    @GetMapping("/all")
    public Result<List<Household>> getAllHouseholds() {
        List<Household> households = householdService.list();
        return Result.success(households);
    }

    /**
     * 根据户籍编号查询
     */
    @ApiOperation(value = "根据户籍编号查询", notes = "根据户籍编号查询户籍信息")
    @GetMapping("/no/{householdNo}")
    public Result<Household> getHouseholdByNo(@ApiParam(value = "户籍编号", required = true) @PathVariable String householdNo) {
        Household household = householdService.getByHouseholdNo(householdNo);
        return Result.success(household);
    }

    /**
     * 删除户籍（逻辑删除）
     */
    @ApiOperation(value = "删除户籍", notes = "逻辑删除指定户籍")
    @DeleteMapping("/{id}")
    public Result<?> deleteHousehold(@ApiParam(value = "户籍ID", required = true) @PathVariable Long id) {
        householdService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 户籍迁出
     */
    @ApiOperation(value = "户籍迁出", notes = "办理户籍迁出手续")
    @PostMapping("/move-out/{id}")
    public Result<?> moveOutHousehold(@ApiParam(value = "户籍ID", required = true) @PathVariable Long id, @RequestBody Map<String, String> params) {
        Household household = householdService.getById(id);
        if (household == null) {
            return Result.error("户籍不存在");
        }
        household.setStatus(0);
        household.setMoveOutDate(java.time.LocalDateTime.now());
        household.setMoveOutReason(params.get("reason"));
        // 清除时间字段，让 MyBatis-Plus 自动填充
        household.setCreateTime(null);
        household.setUpdateTime(null);
        householdService.updateById(household);
        return Result.success("迁出成功");
    }

    /**
     * 导出户籍列表（Excel）
     */
    @ApiOperation(value = "导出户籍列表", notes = "导出户籍列表为Excel文件，支持筛选条件")
    @GetMapping("/export")
    public void exportHouseholds(
            @ApiParam(value = "户籍编号（模糊查询）") @RequestParam(required = false) String householdNo,
            @ApiParam(value = "户主姓名（模糊查询）") @RequestParam(required = false) String headName,
            @ApiParam(value = "户籍地址（模糊查询）") @RequestParam(required = false) String address,
            @ApiParam(value = "状态：0-迁出, 1-正常") @RequestParam(required = false) Integer status,
            HttpServletResponse response) {
        List<HouseholdExcelDTO> data = excelExportService.exportHouseholds(householdNo, headName, address, status);
        ExcelUtil.export(response, "户籍列表", "户籍列表", data, HouseholdExcelDTO.class);
    }
}

