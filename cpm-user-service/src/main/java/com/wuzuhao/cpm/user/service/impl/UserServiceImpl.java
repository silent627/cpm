package com.wuzuhao.cpm.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzuhao.cpm.user.entity.User;
import com.wuzuhao.cpm.user.mapper.UserMapper;
import com.wuzuhao.cpm.user.feign.FileServiceClient;
import com.wuzuhao.cpm.common.dto.ESSyncMessage;
import com.wuzuhao.cpm.config.RabbitMQConfig;
import com.wuzuhao.cpm.user.service.UserService;
import com.wuzuhao.cpm.util.RedisUtil;
import com.wuzuhao.cpm.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String CACHE_PREFIX = "user:";
    private static final String CACHE_BY_ID = CACHE_PREFIX + "id:";
    private static final String CACHE_BY_USERNAME = CACHE_PREFIX + "username:";
    private static final String CACHE_BY_EMAIL = CACHE_PREFIX + "email:";
    private static final long CACHE_EXPIRE_TIME = 3600; // 缓存过期时间：1小时

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Lazy
    private FileServiceClient fileServiceClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    public User getById(Long id) {
        if (id == null) {
            return null;
        }
        String cacheKey = CACHE_BY_ID + id;
        User user = (User) redisUtil.get(cacheKey);
        if (user != null) {
            return user;
        }
        user = super.getById(id);
        if (user != null) {
            redisUtil.set(cacheKey, user, CACHE_EXPIRE_TIME);
        }
        return user;
    }

    @Override
    public User getByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String cacheKey = CACHE_BY_USERNAME + username;
        User user = (User) redisUtil.get(cacheKey);
        if (user != null) {
            return user;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        user = this.getOne(wrapper);
        if (user != null) {
            redisUtil.set(cacheKey, user, CACHE_EXPIRE_TIME);
            // 同时缓存按ID查询的结果
            redisUtil.set(CACHE_BY_ID + user.getId(), user, CACHE_EXPIRE_TIME);
        }
        return user;
    }

    @Override
    public User getByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        String cacheKey = CACHE_BY_EMAIL + email;
        User user = (User) redisUtil.get(cacheKey);
        if (user != null) {
            return user;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        user = this.getOne(wrapper);
        if (user != null) {
            redisUtil.set(cacheKey, user, CACHE_EXPIRE_TIME);
            // 同时缓存按ID和username查询的结果
            redisUtil.set(CACHE_BY_ID + user.getId(), user, CACHE_EXPIRE_TIME);
            if (user.getUsername() != null) {
                redisUtil.set(CACHE_BY_USERNAME + user.getUsername(), user, CACHE_EXPIRE_TIME);
            }
        }
        return user;
    }

    /**
     * 清除用户相关缓存
     */
    private void clearUserCache(User user) {
        if (user != null) {
            redisUtil.del(CACHE_BY_ID + user.getId());
            if (user.getUsername() != null) {
                redisUtil.del(CACHE_BY_USERNAME + user.getUsername());
            }
            if (user.getEmail() != null) {
                redisUtil.del(CACHE_BY_EMAIL + user.getEmail());
            }
        }
    }

    @Override
    public User register(User user) {
        // 检查用户名是否已存在
        User existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 验证手机号格式
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            if (!ValidationUtil.isValidPhone(user.getPhone())) {
                throw new RuntimeException("手机号格式不正确，应为11位数字且以1开头");
            }
            // 检查手机号是否已存在
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, user.getPhone());
            if (this.count(phoneWrapper) > 0) {
                throw new RuntimeException("手机号已被使用");
            }
        }

        // 验证邮箱格式
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            if (!ValidationUtil.isValidEmail(user.getEmail())) {
                throw new RuntimeException("邮箱格式不正确");
            }
            // 检查邮箱是否已存在
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, user.getEmail());
            if (this.count(emailWrapper) > 0) {
                throw new RuntimeException("邮箱已被使用");
            }
        }

        // 验证密码长度（只检查长度，不强制其他规则）
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (!ValidationUtil.validatePasswordLength(user.getPassword())) {
            throw new RuntimeException("密码长度至少8位");
        }

        // 密码加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setStatus(1);
        // createTime 和 updateTime 由 MyBatis-Plus 自动填充
        this.save(user);
        // 注册后清除相关缓存（虽然新数据可能不在缓存中，但为了保持一致性）
        clearUserCache(user);
        // 发送 ES 同步消息
        sendESSyncMessage(ESSyncMessage.create("user_index", user.getId(), convertToMap(user)));
        return user;
    }

    @Override
    public boolean updateUser(User user) {
        // 更新前获取旧数据，用于清除缓存和删除旧图片
        User oldUser = null;
        if (user.getId() != null) {
            oldUser = super.getById(user.getId());
        }

        // 如果更新了头像，删除旧头像
        if (oldUser != null) {
            try {
                fileServiceClient.handleFileUpdate(oldUser.getAvatar(), user.getAvatar());
            } catch (Exception e) {
                // 删除失败不影响更新，记录日志即可
                log.warn("处理头像更新失败: {}", e.getMessage(), e);
            }
        }

        // 验证手机号格式
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            if (!ValidationUtil.isValidPhone(user.getPhone())) {
                throw new RuntimeException("手机号格式不正确，应为11位数字且以1开头");
            }
            // 检查手机号是否已被其他用户使用
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, user.getPhone());
            phoneWrapper.ne(User::getId, user.getId());
            if (this.count(phoneWrapper) > 0) {
                throw new RuntimeException("手机号已被其他用户使用");
            }
        }

        // 验证邮箱格式
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            if (!ValidationUtil.isValidEmail(user.getEmail())) {
                throw new RuntimeException("邮箱格式不正确");
            }
            // 检查邮箱是否已被其他用户使用
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, user.getEmail());
            emailWrapper.ne(User::getId, user.getId());
            if (this.count(emailWrapper) > 0) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
        }

        // updateTime 由 MyBatis-Plus 自动填充
        boolean result = this.updateById(user);
        if (result) {
            // 清除相关缓存
            clearUserCache(oldUser);
            clearUserCache(user);
        }
        return result;
    }

    @Override
    public boolean updateById(User user) {
        // 更新前获取旧数据，用于清除缓存和删除旧图片
        User oldUser = null;
        if (user.getId() != null) {
            oldUser = super.getById(user.getId());
        }

        // 如果更新了头像，删除旧头像
        if (oldUser != null) {
            try {
                fileServiceClient.handleFileUpdate(oldUser.getAvatar(), user.getAvatar());
            } catch (Exception e) {
                // 删除失败不影响更新，记录日志即可
                log.warn("处理头像更新失败: {}", e.getMessage(), e);
            }
        }

        boolean result = super.updateById(user);
        if (result) {
            clearUserCache(oldUser);
            clearUserCache(user);
            // 发送 ES 同步消息
            sendESSyncMessage(ESSyncMessage.update("user_index", user.getId(), convertToMap(user)));
        }
        return result;
    }
    
    /**
     * 发送 ES 同步消息
     */
    private void sendESSyncMessage(ESSyncMessage message) {
        try {
            log.debug("发送 ES 同步消息，operation: {}, index: {}, id: {}", 
                message != null ? message.getOperation() : "null",
                message != null ? message.getIndex() : "null",
                message != null ? message.getId() : "null");
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_SYNC_EXCHANGE,
                RabbitMQConfig.USER_SYNC_ROUTING_KEY,
                message
            );
            log.debug("ES 同步消息发送成功");
        } catch (Exception e) {
            log.error("发送 ES 同步消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 将 User 对象转换为 Map
     */
    private Map<String, Object> convertToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user != null) {
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("realName", user.getRealName());
            map.put("phone", user.getPhone());
            map.put("email", user.getEmail());
            map.put("avatar", user.getAvatar());
            map.put("role", user.getRole());
            map.put("status", user.getStatus());
            // 将 LocalDateTime 转换为字符串格式（yyyy-MM-dd HH:mm:ss），以便前端正确显示
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (user.getCreateTime() != null) {
                map.put("createTime", user.getCreateTime().format(formatter));
            }
            if (user.getUpdateTime() != null) {
                map.put("updateTime", user.getUpdateTime().format(formatter));
            }
        }
        return map;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证邮箱（更改密码需要邮箱验证）
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("更改密码需要先设置邮箱，请联系管理员");
        }
        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            throw new RuntimeException("用户邮箱格式不正确，无法更改密码");
        }

        String oldPasswordMd5 = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!user.getPassword().equals(oldPasswordMd5)) {
            throw new RuntimeException("原密码错误");
        }

        // 验证新密码长度（只检查长度，不强制其他规则）
        if (!ValidationUtil.validatePasswordLength(newPassword)) {
            throw new RuntimeException("新密码长度至少8位");
        }

        // 检查新密码不能与旧密码相同
        String newPasswordMd5 = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        if (user.getPassword().equals(newPasswordMd5)) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }

        user.setPassword(newPasswordMd5);
        // updateTime 由 MyBatis-Plus 自动填充
        boolean result = this.updateById(user);
        if (result) {
            clearUserCache(user);
        }
        return result;
    }

    @Override
    public boolean changePasswordByEmail(Long userId, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证新密码长度（只检查长度，不强制其他规则）
        if (!ValidationUtil.validatePasswordLength(newPassword)) {
            throw new RuntimeException("新密码长度至少8位");
        }

        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        // updateTime 由 MyBatis-Plus 自动填充
        boolean result = this.updateById(user);
        if (result) {
            clearUserCache(user);
        }
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        Long userId = id instanceof Long ? (Long) id : Long.valueOf(id.toString());
        log.info("开始删除用户，id: {}", userId);
        // 删除前获取数据，用于清除缓存
        User user = super.getById(userId);
        if (user == null) {
            log.warn("要删除的用户不存在，id: {}", userId);
            return false;
        }
        boolean result = super.removeById(id);
        log.info("数据库删除操作完成，id: {}, 结果: {}", userId, result);
        if (result) {
            clearUserCache(user);
            // 发送 ES 同步消息
            log.info("用户删除成功，准备发送 ES 删除消息，id: {}", userId);
            sendESSyncMessage(ESSyncMessage.delete("user_index", userId));
            log.info("ES 删除消息已发送，id: {}", userId);
        } else {
            log.warn("数据库删除失败，id: {}", userId);
        }
        return result;
    }
}

