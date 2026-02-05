package com.wuzuhao.cpm.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Feign请求拦截器
 * 用于将当前请求的Token传递给Feign调用
 */
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 从当前请求中获取Authorization header
            String authorization = request.getHeader("Authorization");
            if (authorization != null && !authorization.isEmpty()) {
                // 将Token添加到Feign请求的header中
                template.header("Authorization", authorization);
            }
        }
    }
}
