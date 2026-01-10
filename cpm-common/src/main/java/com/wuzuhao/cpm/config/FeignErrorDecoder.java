package com.wuzuhao.cpm.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Feign错误解码器
 * 用于统一处理Feign调用时的错误响应
 */
@Configuration
public class FeignErrorDecoder implements ErrorDecoder {

    private static final Logger log = LoggerFactory.getLogger(FeignErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        // 记录错误信息
        log.error("Feign调用失败 - Method: {}, Status: {}, Reason: {}", 
                methodKey, response.status(), response.reason());

        // 尝试读取错误响应体
        if (response.body() != null) {
            try {
                InputStream bodyStream = response.body().asInputStream();
                byte[] bytes = new byte[bodyStream.available()];
                bodyStream.read(bytes);
                String body = new String(bytes, StandardCharsets.UTF_8);
                log.error("Feign错误响应体: {}", body);
            } catch (IOException e) {
                log.error("读取Feign错误响应体失败", e);
            }
        }

        // 根据HTTP状态码返回不同的异常
        switch (response.status()) {
            case 400:
                return new RuntimeException("Feign调用失败：请求参数错误");
            case 401:
                return new RuntimeException("Feign调用失败：未授权");
            case 403:
                return new RuntimeException("Feign调用失败：禁止访问");
            case 404:
                return new RuntimeException("Feign调用失败：资源不存在");
            case 500:
                return new RuntimeException("Feign调用失败：服务器内部错误");
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}

