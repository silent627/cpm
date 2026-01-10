package com.wuzuhao.cpm.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.user.dto.excel.AdminExcelDTO;
import com.wuzuhao.cpm.user.entity.Admin;
import com.wuzuhao.cpm.user.entity.User;
import com.wuzuhao.cpm.user.service.AdminService;
import com.wuzuhao.cpm.user.service.ExcelExportService;
import com.wuzuhao.cpm.user.service.UserService;
import com.wuzuhao.cpm.util.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@Api(tags = "系统管理员管理")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private ExcelExportService excelExportService;

    /**
     * 获取当前管理员信息
     */
    @ApiOperation(value = "获取当前管理员信息", notes = "获取当前登录管理员的信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getCurrentAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Admin admin = adminService.getByUserId(userId);
        User user = userService.getById(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("admin", admin);
        result.put("user", user);
        return Result.success(result);
    }

    /**
     * 创建管理员
     */
    @ApiOperation(value = "创建管理员", notes = "创建新的系统管理员账号")
    @PostMapping("/create")
    public Result<Admin> createAdmin(@RequestBody Map<String, Object> params) {
        // 先创建用户
        User user = new User();
        user.setUsername((String) params.get("username"));
        user.setPassword((String) params.get("password"));
        user.setRealName((String) params.get("realName"));
        user.setPhone((String) params.get("phone"));
        user.setEmail((String) params.get("email"));
        user.setRole("ADMIN");
        user = userService.register(user);

        // 创建管理员信息
        Admin admin = new Admin();
        admin.setAdminNo((String) params.get("adminNo"));
        admin.setDepartment((String) params.get("department"));
        admin.setPosition((String) params.get("position"));
        admin.setRemark((String) params.get("remark"));
        admin = adminService.createAdmin(user.getId(), admin);

        return Result.success("创建成功", admin);
    }

    /**
     * 更新管理员信息
     */
    @ApiOperation(value = "更新管理员信息", notes = "更新当前登录管理员的信息")
    @PutMapping("/update")
    public Result<?> updateAdmin(@RequestBody Admin admin, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Admin existAdmin = adminService.getByUserId(userId);
        if (existAdmin != null) {
            admin.setId(existAdmin.getId());
            admin.setUserId(userId);
            adminService.updateById(admin);
            return Result.success("更新成功");
        }
        return Result.error("管理员信息不存在");
    }

    /**
     * 分页查询管理员列表
     */
    @ApiOperation(value = "分页查询管理员列表", notes = "分页查询所有管理员信息")
    @GetMapping("/list")
    public Result<Page<Admin>> getAdminList(
            @ApiParam(value = "当前页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "管理员编号（模糊查询）") @RequestParam(required = false) String adminNo,
            @ApiParam(value = "部门（模糊查询）") @RequestParam(required = false) String department) {
        Page<Admin> page = new Page<>(current, size);
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        if (adminNo != null && !adminNo.isEmpty()) {
            wrapper.like(Admin::getAdminNo, adminNo);
        }
        if (department != null && !department.isEmpty()) {
            wrapper.like(Admin::getDepartment, department);
        }
        wrapper.orderByDesc(Admin::getCreateTime);
        Page<Admin> result = adminService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 根据ID获取管理员
     */
    @ApiOperation(value = "根据ID获取管理员", notes = "根据管理员ID获取详细信息")
    @GetMapping("/{id}")
    public Result<Admin> getAdminById(@ApiParam(value = "管理员ID", required = true) @PathVariable Long id) {
        Admin admin = adminService.getById(id);
        return Result.success(admin);
    }

    /**
     * 导出管理员列表（Excel）
     */
    @ApiOperation(value = "导出管理员列表", notes = "导出管理员列表为Excel文件，支持筛选条件")
    @GetMapping("/export")
    public void exportAdmins(
            @ApiParam(value = "管理员编号（模糊查询）") @RequestParam(required = false) String adminNo,
            @ApiParam(value = "部门（模糊查询）") @RequestParam(required = false) String department,
            HttpServletResponse response) {
        List<AdminExcelDTO> data = excelExportService.exportAdmins(adminNo, department);
        ExcelUtil.export(response, "管理员列表", "管理员列表", data, AdminExcelDTO.class);
    }
}

