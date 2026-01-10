package com.wuzuhao.cpm.notification.service.impl;

import com.wuzuhao.cpm.notification.service.EmailCodeService;
import com.wuzuhao.cpm.notification.service.EmailService;
import com.wuzuhao.cpm.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码服务实现类
 */
@Service
public class EmailCodeServiceImpl implements EmailCodeService {

    private static final Logger log = LoggerFactory.getLogger(EmailCodeServiceImpl.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String EMAIL_CODE_PREFIX = "email_code:";
    private static final String EMAIL_SEND_LOCK_PREFIX = "email_send_lock:";
    private static final int CODE_LENGTH = 6; // 验证码长度
    private static final int CODE_EXPIRE_MINUTES = 5; // 验证码有效期（分钟）
    private static final int SEND_INTERVAL_SECONDS = 60; // 发送间隔（秒）

    @Override
    public boolean sendEmailCode(String email, String type) {
        // 验证邮箱格式
        if (!ValidationUtil.isValidEmail(email)) {
            log.warn("邮箱格式不正确: {}", email);
            return false;
        }

        // 检查是否可以发送（60秒间隔限制）
        if (!canSendCode(email, type)) {
            log.warn("发送验证码过于频繁: {} (type: {})", email, type);
            return false;
        }

        // 生成6位数字验证码
        String code = generateCode();
        String key = EMAIL_CODE_PREFIX + type + ":" + email;
        String lockKey = EMAIL_SEND_LOCK_PREFIX + type + ":" + email;

        // 将验证码存入Redis，5分钟过期
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 设置发送锁，60秒过期
        redisTemplate.opsForValue().set(lockKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 发送邮件（baseUrl可以为空，用于生成自动验证链接，当前版本暂不使用）
        boolean success = emailService.sendVerificationCode(email, code, type, "");
        if (success) {
            log.info("邮箱验证码发送成功: {} (type: {}), 验证码: {}", email, type, code);
        } else {
            log.error("邮箱验证码发送失败: {} (type: {}), 验证码: {} (已保存到Redis，但邮件发送失败)", email, type, code);
            log.warn("【开发模式】验证码已生成并保存到Redis，验证码: {}，邮箱: {}，类型: {}", code, email, type);
            log.warn("【重要】验证码已保存，即使邮件发送失败，您仍可以使用此验证码进行测试（有效期5分钟）");
        }

        return success;
    }

    @Override
    public boolean validateEmailCode(String email, String code, String type) {
        if (email == null || code == null || type == null) {
            return false;
        }

        String key = EMAIL_CODE_PREFIX + type + ":" + email;
        String storedCode = (String) redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("验证码不存在或已过期: {} (type: {})", email, type);
            return false;
        }

        boolean valid = storedCode.equals(code.trim());
        if (valid) {
            // 验证成功后删除验证码（一次性使用）
            redisTemplate.delete(key);
            log.info("邮箱验证码验证成功: {} (type: {})", email, type);
        } else {
            log.warn("邮箱验证码验证失败: {} (type: {}), 输入: {}, 正确: {}", email, type, code, storedCode);
        }

        return valid;
    }

    @Override
    public boolean verifyEmailCodeOnly(String email, String code, String type) {
        if (email == null || code == null || type == null) {
            return false;
        }

        String key = EMAIL_CODE_PREFIX + type + ":" + email;
        String storedCode = (String) redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            return false;
        }

        boolean valid = storedCode.equals(code.trim());
        if (!valid) {
            log.warn("邮箱验证码验证失败: {} (type: {}), 输入: {}, 正确: {}", email, type, code, storedCode);
        }

        return valid;
    }

    @Override
    public boolean canSendCode(String email, String type) {
        String lockKey = EMAIL_SEND_LOCK_PREFIX + type + ":" + email;
        return redisTemplate.opsForValue().get(lockKey) == null;
    }

    @Override
    public long getRemainingSeconds(String email, String type) {
        String lockKey = EMAIL_SEND_LOCK_PREFIX + type + ":" + email;
        Long expire = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return expire != null && expire > 0 ? expire : 0;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
