package com.wuzuhao.cpm.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign统一配置类
 * 用于配置Feign客户端的超时、重试、日志等参数
 */
@Configuration
public class FeignConfig {

    /**
     * 配置Feign日志级别
     * NONE: 不输出日志（默认）
     * BASIC: 只输出请求方法和URL以及响应状态码和执行时间
     * HEADERS: 输出BASIC级别信息以及请求和响应的头信息
     * FULL: 输出完整的请求和响应信息
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * 配置Feign请求超时时间
     * connectTimeoutMillis: 连接超时时间（毫秒）
     * readTimeoutMillis: 读取超时时间（毫秒）
     * 
     * 注意：Request.Options构造函数已弃用，但Spring Cloud OpenFeign仍支持此方式
     * 未来版本建议通过配置文件(application.yml)中的feign.client.config来设置超时时间
     * 例如：feign.client.config.default.connectTimeout=5000, readTimeout=10000
     */
    @Bean
    @SuppressWarnings("deprecation")
    public Request.Options requestOptions() {
        return new Request.Options(
            5000,  // 连接超时时间：5秒
            10000  // 读取超时时间：10秒
        );
    }

    /**
     * 配置Feign重试机制
     * period: 初始重试间隔时间（毫秒）
     * maxPeriod: 最大重试间隔时间（毫秒）
     * maxAttempts: 最大重试次数（包括第一次请求）
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
                100,      // 初始重试间隔：100毫秒
                1000,     // 最大重试间隔：1秒
                3         // 最大重试次数：3次（总共尝试4次）
        );
    }

    /**
     * 配置Feign请求拦截器
     * 用于将当前请求的Token传递给Feign调用
     */
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }
}

