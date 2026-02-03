package com.wuzuhao.cpm.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.user.dto.excel.AdminExcelDTO;
import com.wuzuhao.cpm.user.entity.Admin;
import com.wuzuhao.cpm.user.entity.User;
import com.wuzuhao.cpm.user.feign.SearchServiceClient;
import com.wuzuhao.cpm.user.service.AdminService;
import com.wuzuhao.cpm.user.service.ExcelExportService;
import com.wuzuhao.cpm.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import com.wuzuhao.cpm.util.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@Slf4j
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

    @Autowired
    private SearchServiceClient searchServiceClient;

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
            // 清除时间字段，让 MyBatis-Plus 自动填充
            admin.setCreateTime(null);
            admin.setUpdateTime(null);
            adminService.updateById(admin);
            return Result.success("更新成功");
        }
        return Result.error("管理员信息不存在");
    }

    /**
     * 获取所有管理员列表（用于索引同步）
     */
    @ApiOperation(value = "获取所有管理员列表", notes = "获取所有管理员信息，用于搜索服务索引同步")
    @GetMapping("/all")
    public Result<List<Admin>> getAllAdmins() {
        List<Admin> admins = adminService.list();
        return Result.success(admins);
    }

    /**
     * 分页查询管理员列表（使用Elasticsearch全文检索）
     */
    @ApiOperation(value = "分页查询管理员列表", notes = "使用Elasticsearch全文检索查询管理员信息")
    @GetMapping("/list")
    public Result<Page<Admin>> getAdminList(
            @ApiParam(value = "当前页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "管理员编号（模糊查询）") @RequestParam(required = false) String adminNo,
            @ApiParam(value = "部门（模糊查询）") @RequestParam(required = false) String department) {
        try {
            // 合并查询参数为keyword
            StringBuilder keywordBuilder = new StringBuilder();
            if (adminNo != null && !adminNo.trim().isEmpty()) {
                keywordBuilder.append(adminNo.trim());
            }
            if (department != null && !department.trim().isEmpty()) {
                if (keywordBuilder.length() > 0) {
                    keywordBuilder.append(" ");
                }
                keywordBuilder.append(department.trim());
            }
            
            String keyword = keywordBuilder.length() > 0 ? keywordBuilder.toString() : "*";
            
            // 调用搜索服务（page参数从1开始，Elasticsearch从0开始，需要减1）
            Result<Map<String, Object>> searchResult = searchServiceClient.searchAdmin(keyword, current - 1, size);
            
            if (searchResult == null || searchResult.getCode() != 200 || searchResult.getData() == null) {
                // 如果搜索服务失败，返回空结果
                Page<Admin> page = new Page<>(current, size);
                page.setTotal(0);
                return Result.success(page);
            }
            
            Map<String, Object> data = searchResult.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> hits = (List<Map<String, Object>>) data.get("hits");
            Long total = ((Number) data.get("total")).longValue();
            
            // 转换为Admin实体列表
            List<Admin> admins = new ArrayList<>();
            if (hits != null) {
                for (Map<String, Object> hit : hits) {
                    try {
                        Admin admin = convertMapToAdmin(hit);
                        admins.add(admin);
                    } catch (Exception e) {
                        // 忽略转换失败的数据
                        continue;
                    }
                }
            }
            
            // 构建Page对象
            Page<Admin> page = new Page<>(current, size);
            page.setTotal(total);
            page.setRecords(admins);
            return Result.success(page);
            
        } catch (Exception e) {
            log.error("搜索管理员信息失败", e);
            // 如果搜索服务异常，返回空结果
            Page<Admin> page = new Page<>(current, size);
            page.setTotal(0);
            return Result.success(page);
        }
    }

    /**
     * 将Map转换为Admin对象
     */
    private Admin convertMapToAdmin(Map<String, Object> map) {
        Admin admin = new Admin();
        if (map.get("id") != null) {
            admin.setId(Long.valueOf(map.get("id").toString()));
        }
        if (map.get("userId") != null) {
            admin.setUserId(Long.valueOf(map.get("userId").toString()));
        }
        admin.setAdminNo((String) map.get("adminNo"));
        admin.setDepartment((String) map.get("department"));
        admin.setPosition((String) map.get("position"));
        admin.setRemark((String) map.get("remark"));
        
        // 使用 DateTimeFormatter 指定日期格式 "yyyy-MM-dd HH:mm:ss"
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        if (map.get("createTime") != null) {
            try {
                String createTimeStr = map.get("createTime").toString();
                admin.setCreateTime(java.time.LocalDateTime.parse(createTimeStr, formatter));
                log.debug("成功解析管理员 createTime: {}", createTimeStr);
            } catch (Exception e) {
                log.warn("解析管理员 createTime 失败: {}, 错误: {}", map.get("createTime"), e.getMessage());
                // 忽略日期解析错误，继续处理其他字段
            }
        }
        if (map.get("updateTime") != null) {
            try {
                String updateTimeStr = map.get("updateTime").toString();
                admin.setUpdateTime(java.time.LocalDateTime.parse(updateTimeStr, formatter));
                log.debug("成功解析管理员 updateTime: {}", updateTimeStr);
            } catch (Exception e) {
                log.warn("解析管理员 updateTime 失败: {}, 错误: {}", map.get("updateTime"), e.getMessage());
                // 忽略日期解析错误，继续处理其他字段
            }
        }
        return admin;
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

