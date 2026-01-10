package com.wuzuhao.cpm.household.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.household.dto.excel.HouseholdExcelDTO;
import com.wuzuhao.cpm.household.entity.Household;
import com.wuzuhao.cpm.household.service.ExcelExportService;
import com.wuzuhao.cpm.household.service.HouseholdService;
import com.wuzuhao.cpm.util.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 户籍控制器
 */
@Api(tags = "户籍管理")
@RestController
@RequestMapping("/household")
public class HouseholdController {

    @Autowired
    private HouseholdService householdService;

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
        householdService.updateById(household);
        return Result.success("更新成功");
    }

    /**
     * 分页查询户籍列表
     */
    @ApiOperation(value = "分页查询户籍列表", notes = "分页查询所有户籍信息")
    @GetMapping("/list")
    public Result<Page<Household>> getHouseholdList(
            @ApiParam(value = "当前页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "户籍编号（模糊查询）") @RequestParam(required = false) String householdNo,
            @ApiParam(value = "户主姓名（模糊查询）") @RequestParam(required = false) String headName,
            @ApiParam(value = "户籍地址（模糊查询）") @RequestParam(required = false) String address,
            @ApiParam(value = "状态：0-迁出, 1-正常") @RequestParam(required = false) Integer status) {
        Page<Household> page = new Page<>(current, size);
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
        Page<Household> result = householdService.page(page, wrapper);
        return Result.success(result);
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

