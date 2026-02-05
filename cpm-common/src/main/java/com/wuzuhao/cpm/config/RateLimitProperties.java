package com.wuzuhao.cpm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 限流配置属性
 */
@Component
@ConfigurationProperties(prefix = "cpm.rate-limit")
public class RateLimitProperties {

    /**
     * 是否启用限流，默认启用
     * 测试时可以设置为 false 来禁用所有限流
     */
    private boolean enabled = true;

    /**
     * 每分钟最大请求数，默认180
     */
    private int maxRequestsPerMinute = 180;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxRequestsPerMinute() {
        return maxRequestsPerMinute;
    }

    public void setMaxRequestsPerMinute(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }
}
