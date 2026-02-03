package com.wuzuhao.cpm.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.user.dto.excel.UserExcelDTO;
import com.wuzuhao.cpm.user.entity.User;
import com.wuzuhao.cpm.user.service.ExcelExportService;
import com.wuzuhao.cpm.user.feign.NotificationServiceClient;
import com.wuzuhao.cpm.user.feign.SearchServiceClient;
import com.wuzuhao.cpm.user.service.UserService;
import com.wuzuhao.cpm.util.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    @Lazy
    private NotificationServiceClient notificationServiceClient;

    @Autowired
    @Lazy
    private SearchServiceClient searchServiceClient;

    /**
     * 用户注册
     */
    @ApiOperation(value = "用户注册", notes = "新用户注册，无需登录")
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        User result = userService.register(user);
        result.setPassword(null); // 不返回密码
        return Result.success("注册成功", result);
    }

    /**
     * 获取当前用户信息
     */
    @ApiOperation(value = "获取当前用户信息", notes = "获取当前登录用户的信息")
    @GetMapping("/info")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @ApiOperation(value = "更新用户信息", notes = "更新当前登录用户的信息")
    @PutMapping("/update")
    public Result<?> updateUser(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        user.setPassword(null); // 不允许通过此接口修改密码
        // 清除时间字段，让 MyBatis-Plus 自动填充
        user.setCreateTime(null);
        user.setUpdateTime(null);
        userService.updateUser(user);
        return Result.success("更新成功");
    }

    /**
     * 发送修改密码验证码
     */
    @ApiOperation(value = "发送修改密码验证码", notes = "向当前用户邮箱发送验证码，用于修改密码")
    @PostMapping("/change-password/send-code")
    public Result<?> sendChangePasswordCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return Result.error("用户未设置邮箱，无法发送验证码");
        }

        String email = user.getEmail();
        
        // 检查是否可以发送（60秒间隔）
        Result<Boolean> canSendResult = notificationServiceClient.canSendCode(email, "change");
        if (canSendResult.getCode() != 200 || !canSendResult.getData()) {
            Result<Long> remainingResult = notificationServiceClient.getRemainingSeconds(email, "change");
            long remaining = remainingResult.getCode() == 200 ? remainingResult.getData() : 60;
            return Result.error("发送过于频繁，请" + remaining + "秒后再试");
        }

        Result<?> sendResult = notificationServiceClient.sendEmailCode(email, "change");
        boolean success = sendResult.getCode() == 200;
        if (success) {
            return Result.success("验证码已发送，请查收邮件");
        } else {
            return Result.error("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 修改密码
     */
    @ApiOperation(value = "修改密码", notes = "修改当前登录用户的密码，支持两种方式：1. 通过旧密码验证 2. 通过邮箱验证码验证")
    @PostMapping("/change-password")
    public Result<?> changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        String emailCode = params.get("emailCode");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error("新密码不能为空");
        }

        // 判断使用哪种验证方式
        boolean useEmailCode = emailCode != null && !emailCode.trim().isEmpty();
        boolean useOldPassword = oldPassword != null && !oldPassword.trim().isEmpty();

        if (!useEmailCode && !useOldPassword) {
            return Result.error("请选择验证方式：输入旧密码或邮箱验证码");
        }

        if (useEmailCode && useOldPassword) {
            return Result.error("请只选择一种验证方式：旧密码或邮箱验证码");
        }

        // 通过邮箱验证码验证
        if (useEmailCode) {
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return Result.error("用户未设置邮箱，无法使用邮箱验证码修改密码");
            }
            
            // 验证邮箱验证码
            Result<?> validateResult = notificationServiceClient.validateEmailCode(user.getEmail(), emailCode, "change");
            if (validateResult.getCode() != 200) {
                return Result.error("邮箱验证码错误或已过期");
            }

            // 通过邮箱验证码修改密码（不需要旧密码）
            try {
                userService.changePasswordByEmail(userId, newPassword);
                return Result.success("密码修改成功");
            } catch (Exception e) {
                return Result.error(e.getMessage());
            }
        }

        // 通过旧密码验证
        if (useOldPassword) {
            // 修改密码（需要验证旧密码）
            try {
                userService.changePassword(userId, oldPassword, newPassword);
                return Result.success("密码修改成功");
            } catch (Exception e) {
                return Result.error(e.getMessage());
            }
        }

        return Result.error("验证方式错误");
    }

    /**
     * 获取所有用户列表（用于索引同步）
     */
    @ApiOperation(value = "获取所有用户列表", notes = "获取所有用户信息，用于搜索服务索引同步")
    @GetMapping("/all")
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.list();
        // 清除密码
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    /**
     * 分页查询用户列表（管理员，使用Elasticsearch全文检索）
     */
    @ApiOperation(value = "分页查询用户列表", notes = "管理员功能，使用Elasticsearch全文检索查询用户信息")
    @GetMapping("/list")
    public Result<Page<User>> getUserList(
            @ApiParam(value = "当前页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "用户名（模糊查询）") @RequestParam(required = false) String username,
            @ApiParam(value = "角色") @RequestParam(required = false) String role) {
        try {
            // 合并查询参数为keyword
            StringBuilder keywordBuilder = new StringBuilder();
            if (username != null && !username.trim().isEmpty()) {
                keywordBuilder.append(username.trim());
            }
            
            String keyword = keywordBuilder.length() > 0 ? keywordBuilder.toString() : "*";
            
            // 调用搜索服务（page参数从1开始，Elasticsearch从0开始，需要减1）
            Result<Map<String, Object>> searchResult = searchServiceClient.searchUser(keyword, current - 1, size);
            
            if (searchResult == null || searchResult.getCode() != 200 || searchResult.getData() == null) {
                // 如果搜索服务失败，返回空结果
                Page<User> page = new Page<>(current, size);
                page.setTotal(0);
                return Result.success(page);
            }
            
            Map<String, Object> data = searchResult.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> hits = (List<Map<String, Object>>) data.get("hits");
            Long total = ((Number) data.get("total")).longValue();
            
            // 转换为User实体列表
            List<User> users = new ArrayList<>();
            if (hits != null) {
                for (Map<String, Object> hit : hits) {
                    try {
                        // 过滤已删除的数据
                        Object deletedObj = hit.get("deleted");
                        if (deletedObj != null) {
                            int deleted = deletedObj instanceof Number ? 
                                ((Number) deletedObj).intValue() : 
                                Integer.parseInt(deletedObj.toString());
                            if (deleted == 1) {
                                continue; // 跳过已删除的数据
                            }
                        }
                        User user = convertMapToUser(hit);
                        // 如果指定了role参数，进行过滤
                        if (role != null && !role.isEmpty() && user.getRole() != null && !user.getRole().equals(role)) {
                            continue;
                        }
                        users.add(user);
                    } catch (Exception e) {
                        // 忽略转换失败的数据
                        continue;
                    }
                }
            }
            
            // 构建Page对象
            Page<User> page = new Page<>(current, size);
            page.setTotal(total);
            page.setRecords(users);
            return Result.success(page);
            
        } catch (Exception e) {
            log.error("搜索用户信息失败", e);
            // 如果搜索服务异常，返回空结果
        Page<User> page = new Page<>(current, size);
            page.setTotal(0);
            return Result.success(page);
        }
    }

    /**
     * 将Map转换为User对象
     */
    private User convertMapToUser(Map<String, Object> map) {
        User user = new User();
        if (map.get("id") != null) {
            user.setId(Long.valueOf(map.get("id").toString()));
        }
        user.setUsername((String) map.get("username"));
        user.setRealName((String) map.get("realName"));
        user.setPhone((String) map.get("phone"));
        user.setEmail((String) map.get("email"));
        user.setAvatar((String) map.get("avatar"));
        user.setRole((String) map.get("role"));
        if (map.get("status") != null) {
            user.setStatus(Integer.valueOf(map.get("status").toString()));
        }
        
        // 使用 DateTimeFormatter 指定日期格式 "yyyy-MM-dd HH:mm:ss"
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        if (map.get("createTime") != null) {
            try {
                String createTimeStr = map.get("createTime").toString();
                user.setCreateTime(java.time.LocalDateTime.parse(createTimeStr, formatter));
                log.debug("成功解析用户 createTime: {}", createTimeStr);
            } catch (Exception e) {
                log.warn("解析用户 createTime 失败: {}, 错误: {}", map.get("createTime"), e.getMessage());
                // 忽略日期解析错误，继续处理其他字段
            }
        }
        if (map.get("updateTime") != null) {
            try {
                String updateTimeStr = map.get("updateTime").toString();
                user.setUpdateTime(java.time.LocalDateTime.parse(updateTimeStr, formatter));
                log.debug("成功解析用户 updateTime: {}", updateTimeStr);
            } catch (Exception e) {
                log.warn("解析用户 updateTime 失败: {}, 错误: {}", map.get("updateTime"), e.getMessage());
                // 忽略日期解析错误，继续处理其他字段
            }
        }
        return user;
    }

    /**
     * 根据ID获取用户（管理员）
     */
    @ApiOperation(value = "根据ID获取用户", notes = "管理员功能，根据用户ID获取用户信息")
    @GetMapping("/{id}")
    public Result<User> getUserById(@ApiParam(value = "用户ID", required = true) @PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 更新用户状态（管理员）
     */
    @ApiOperation(value = "更新用户状态", notes = "管理员功能，启用或禁用用户")
    @PutMapping("/status/{id}")
    public Result<?> updateUserStatus(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long id,
            @RequestBody Map<String, Integer> params) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(params.get("status"));
        // 清除时间字段，让 MyBatis-Plus 自动填充
        user.setCreateTime(null);
        user.setUpdateTime(null);
        // 直接调用 updateById，避免触发手机号和邮箱的重复检查
        // updateById 会发送 ES 同步消息
        boolean result = userService.updateById(user);
        if (result) {
            return Result.success("状态更新成功");
        } else {
            return Result.error("状态更新失败");
        }
    }

    /**
     * 更新用户信息（管理员，根据ID）
     */
    @ApiOperation(value = "更新用户信息", notes = "管理员功能，根据用户ID更新用户信息")
    @PutMapping("/update/{id}")
    public Result<?> updateUserById(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long id,
            @RequestBody User user) {
        User existUser = userService.getById(id);
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        user.setId(id);
        user.setPassword(null); // 不允许通过此接口修改密码
        // 清除时间字段，让 MyBatis-Plus 自动填充
        user.setCreateTime(null);
        user.setUpdateTime(null);
        userService.updateUser(user);
        return Result.success("更新成功");
    }

    /**
     * 删除用户（管理员，逻辑删除）
     */
    @ApiOperation(value = "删除用户", notes = "管理员功能，逻辑删除用户")
    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@ApiParam(value = "用户ID", required = true) @PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        userService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 导出用户列表（Excel）
     */
    @ApiOperation(value = "导出用户列表", notes = "管理员功能，导出用户列表为Excel文件，支持筛选条件")
    @GetMapping("/export")
    public void exportUsers(
            @ApiParam(value = "用户名（模糊查询）") @RequestParam(required = false) String username,
            @ApiParam(value = "角色") @RequestParam(required = false) String role,
            HttpServletResponse response) {
        List<UserExcelDTO> data = excelExportService.exportUsers(username, role);
        ExcelUtil.export(response, "用户列表", "用户列表", data, UserExcelDTO.class);
    }

    /**
     * 批量删除用户
     */
    @ApiOperation(value = "批量删除用户", notes = "管理员功能，批量删除用户")
    @PostMapping("/batch/delete")
    public Result<Integer> batchDeleteUsers(@RequestBody Map<String, List<Long>> params) {
        List<Long> ids = params.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的用户");
        }
        boolean success = userService.removeByIds(ids);
        int count = success ? ids.size() : 0;
        return Result.success("批量删除成功", count);
    }

}

