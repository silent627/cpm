package com.wuzuhao.cpm.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuzuhao.cpm.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 限流拦截器（防刷）
 * 通用模块，供各微服务使用
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Autowired
    @NonNull
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @NonNull
    private ObjectMapper objectMapper;

    // 限流配置
    private static final int MAX_REQUESTS_PER_MINUTE = 60; // 每分钟最大请求数
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        try {
            // 获取客户端IP
            String clientIp = getClientIp(request);
            if (clientIp == null || clientIp.isEmpty()) {
                // 如果无法获取IP，使用默认值
                clientIp = "unknown";
            }
            String key = RATE_LIMIT_PREFIX + clientIp;

            // 获取当前请求次数
            Integer count = (Integer) redisTemplate.opsForValue().get(key);
            if (count == null) {
                count = 0;
            }

            // 检查是否超过限制
            if (count >= MAX_REQUESTS_PER_MINUTE) {
                log.warn("IP {} 请求过于频繁，已限流", clientIp);
                sendErrorResponse(response, "请求过于频繁，请稍后再试");
                return false;
            }

            // 增加请求次数
            count++;
            redisTemplate.opsForValue().set(key, count, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            // Redis连接失败或其他异常时，记录日志但允许请求通过，避免因Redis问题导致服务不可用
            log.error("限流拦截器执行异常，允许请求通过: {}", e.getMessage(), e);
            // 不抛出异常，允许请求继续处理
        }

        return true;
    }

    /**
     * 获取客户端真实IP
     * @return IP地址，如果无法获取则返回null
     */
    @Nullable
    private String getClientIp(@NonNull HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(@NonNull HttpServletResponse response, @NonNull String message) throws IOException {
        response.setStatus(429); // 429 Too Many Requests
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

