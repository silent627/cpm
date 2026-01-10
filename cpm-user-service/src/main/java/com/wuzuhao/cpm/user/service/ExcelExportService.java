package com.wuzuhao.cpm.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wuzuhao.cpm.user.dto.excel.AdminExcelDTO;
import com.wuzuhao.cpm.user.dto.excel.UserExcelDTO;
import com.wuzuhao.cpm.user.entity.Admin;
import com.wuzuhao.cpm.user.entity.User;
import com.wuzuhao.cpm.user.service.impl.AdminServiceImpl;
import com.wuzuhao.cpm.user.service.impl.UserServiceImpl;
import com.wuzuhao.cpm.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Excel导出服务（用户服务专用）
 */
@Service
public class ExcelExportService {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private AdminServiceImpl adminService;

    /**
     * 导出用户列表
     */
    public List<UserExcelDTO> exportUsers(String username, String role) {
        // 查询所有符合条件的用户（不分页）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreateTime);
        
        List<User> users = userService.list(wrapper);
        
        // 转换为Excel DTO
        return users.stream().map(user -> {
            UserExcelDTO dto = new UserExcelDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setRealName(user.getRealName());
            dto.setPhone(user.getPhone());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole() != null && user.getRole().equals("ADMIN") ? "管理员" : "普通用户");
            dto.setStatus(user.getStatus() != null && user.getStatus() == 1 ? "启用" : "禁用");
            dto.setCreateTime(ExcelUtil.formatDateTime(user.getCreateTime()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 导出管理员列表
     */
    public List<AdminExcelDTO> exportAdmins(String adminNo, String department) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        if (adminNo != null && !adminNo.isEmpty()) {
            wrapper.like(Admin::getAdminNo, adminNo);
        }
        if (department != null && !department.isEmpty()) {
            wrapper.like(Admin::getDepartment, department);
        }
        wrapper.orderByDesc(Admin::getCreateTime);
        
        List<Admin> admins = adminService.list(wrapper);
        
        // 转换为Excel DTO，需要关联用户信息
        return admins.stream().map(admin -> {
            AdminExcelDTO dto = new AdminExcelDTO();
            dto.setId(admin.getId());
            dto.setAdminNo(admin.getAdminNo());
            dto.setDepartment(admin.getDepartment());
            dto.setPosition(admin.getPosition());
            dto.setRemark(admin.getRemark());
            dto.setCreateTime(ExcelUtil.formatDateTime(admin.getCreateTime()));
            
            // 获取关联的用户信息
            if (admin.getUserId() != null) {
                User user = userService.getById(admin.getUserId());
                if (user != null) {
                    dto.setUsername(user.getUsername());
                    dto.setRealName(user.getRealName());
                    dto.setPhone(user.getPhone());
                    dto.setEmail(user.getEmail());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
}

