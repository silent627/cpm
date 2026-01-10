package com.wuzuhao.cpm.file.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    /**
     * 配置静态资源访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String basePath = uploadPath;
        if (!basePath.startsWith("/") && !basePath.contains(":")) {
            String userDir = System.getProperty("user.dir");
            if (userDir == null || userDir.isEmpty()) {
                userDir = "/app";
            }
            basePath = userDir + "/" + basePath;
        }
        if (!basePath.endsWith("/")) {
            basePath += "/";
        }
        log.info("配置静态资源访问 - urlPrefix: {}, uploadPath: {}, basePath: {}", urlPrefix, uploadPath, basePath);
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations("file:" + basePath)
                .setCachePeriod(3600);
    }
}
