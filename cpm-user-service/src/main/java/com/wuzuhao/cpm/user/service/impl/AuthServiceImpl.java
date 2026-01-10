package com.wuzuhao.cpm.user.service.impl;

import com.wuzuhao.cpm.user.dto.LoginDTO;
import com.wuzuhao.cpm.user.dto.LoginResultDTO;
import com.wuzuhao.cpm.user.entity.User;
import com.wuzuhao.cpm.user.service.AuthService;
import com.wuzuhao.cpm.user.feign.NotificationServiceClient;
import com.wuzuhao.cpm.user.service.UserService;
import com.wuzuhao.cpm.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Lazy
    private NotificationServiceClient notificationServiceClient;

    /**
     * 是否启用登录验证码
     * 默认 false：开发调试阶段关闭验证码校验
     * 如需开启，在配置文件中设置 login.captcha.enabled=true
     */
    @Value("${login.captcha.enabled:false}")
    private boolean loginCaptchaEnabled;

    private static final String REDIS_TOKEN_PREFIX = "token:";
    private static final String LOGIN_FAIL_PREFIX = "login_fail:";
    private static final int MAX_LOGIN_FAIL_COUNT = 5; // 最大登录失败次数
    private static final int LOCK_TIME_MINUTES = 30; // 锁定时间（分钟）

    @Override
    public LoginResultDTO login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String failKey = LOGIN_FAIL_PREFIX + username;
        
        // 检查是否被锁定
        Integer failCount = (Integer) redisTemplate.opsForValue().get(failKey);
        if (failCount != null && failCount >= MAX_LOGIN_FAIL_COUNT) {
            Long expire = redisTemplate.getExpire(failKey, TimeUnit.SECONDS);
            throw new RuntimeException("账户已被锁定，请" + (expire / 60) + "分钟后再试");
        }

        // 验证验证码（开发调试阶段可通过配置关闭）
        if (loginCaptchaEnabled) {
            if (loginDTO.getCaptchaKey() != null && loginDTO.getCaptchaCode() != null) {
                com.wuzuhao.cpm.common.Result<?> validateResult = notificationServiceClient.validateCaptcha(
                    loginDTO.getCaptchaKey(), loginDTO.getCaptchaCode());
                if (validateResult.getCode() != 200) {
                    incrementLoginFailCount(username);
                    throw new RuntimeException("验证码错误");
                }
            } else {
                // 如果失败次数达到3次，要求输入验证码
                if (failCount != null && failCount >= 3) {
                    throw new RuntimeException("登录失败次数过多，请输入验证码");
                }
            }
        }

        // 查询用户
        User user = userService.getByUsername(username);
        if (user == null) {
            incrementLoginFailCount(username);
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        String passwordMd5 = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!user.getPassword().equals(passwordMd5)) {
            incrementLoginFailCount(username);
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 登录成功，清除失败次数
        redisTemplate.delete(failKey);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 将Token存入Redis，设置过期时间
        redisTemplate.opsForValue().set(REDIS_TOKEN_PREFIX + user.getId(), token, 24, TimeUnit.HOURS);

        // 构建返回结果
        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        result.setUserId(user.getId());
        result.setUsername(user.getUsername());
        result.setRole(user.getRole());
        result.setRealName(user.getRealName());

        return result;
    }

    /**
     * 增加登录失败次数
     */
    private void incrementLoginFailCount(String username) {
        String failKey = LOGIN_FAIL_PREFIX + username;
        Integer failCount = (Integer) redisTemplate.opsForValue().get(failKey);
        if (failCount == null) {
            failCount = 0;
        }
        failCount++;
        
        if (failCount >= MAX_LOGIN_FAIL_COUNT) {
            // 锁定账户
            redisTemplate.opsForValue().set(failKey, failCount, LOCK_TIME_MINUTES, TimeUnit.MINUTES);
        } else {
            // 设置过期时间
            redisTemplate.opsForValue().set(failKey, failCount, LOCK_TIME_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Override
    public void logout(String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            redisTemplate.delete(REDIS_TOKEN_PREFIX + userId);
        } catch (Exception e) {
            // 忽略异常
        }
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // 验证Token是否有效
        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        // 检查Redis中是否存在Token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            Object redisToken = redisTemplate.opsForValue().get(REDIS_TOKEN_PREFIX + userId);
            return token.equals(redisToken);
        } catch (Exception e) {
            return false;
        }
    }
}

