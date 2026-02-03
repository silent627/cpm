package com.wuzuhao.cpm.household.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuzuhao.cpm.config.RateLimitProperties;
import com.wuzuhao.cpm.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    private RateLimitProperties rateLimitProperties;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 配置消息转换器，使用 JacksonConfig 中配置的 ObjectMapper
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        converters.add(0, converter);
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 限流拦截器 - 根据配置决定是否启用
        if (rateLimitProperties.isEnabled()) {
            registry.addInterceptor(rateLimitInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(
                            "/household/all",  // 排除索引同步接口，避免性能测试时触发限流
                            "/household-member/all",  // 排除索引同步接口
                            "/doc.html",
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v2/api-docs",
                            "/swagger-resources/**",
                            "/webjars/**"
                    )
                    .order(1);
        }
    }

    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

