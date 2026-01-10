package com.wuzuhao.cpm.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.user.dto.excel.UserExcelDTO;
import com.wuzuhao.cpm.user.entity.User;
import com.wuzuhao.cpm.user.service.ExcelExportService;
import com.wuzuhao.cpm.user.feign.NotificationServiceClient;
import com.wuzuhao.cpm.user.service.UserService;
import com.wuzuhao.cpm.util.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
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
     * 分页查询用户列表（管理员）
     */
    @ApiOperation(value = "分页查询用户列表", notes = "管理员功能，分页查询所有用户")
    @GetMapping("/list")
    public Result<Page<User>> getUserList(
            @ApiParam(value = "当前页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "用户名（模糊查询）") @RequestParam(required = false) String username,
            @ApiParam(value = "角色") @RequestParam(required = false) String role) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userService.page(page, wrapper);
        // 清除密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
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
        userService.updateUser(user);
        return Result.success("状态更新成功");
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

