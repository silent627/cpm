package com.wuzuhao.cpm.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * 静态资源配置类
 * 用于在 Spring Cloud Gateway 中提供前端静态资源服务（SPA）
 * 
 * 注意：暂时禁用 RouterFunction，因为它会拦截所有请求，导致 Gateway 路由无法正常工作
 * 静态资源可以通过 Gateway 的过滤器或直接访问来提供
 */
@Configuration
public class StaticResourceConfig {

    /**
     * 配置静态资源路由
     * 支持前端 SPA 应用的路由
     * 
     * 暂时禁用，避免拦截 Gateway 路由
     */
    // @Bean
    // public RouterFunction<ServerResponse> staticResourceRouter() {
    //     return RouterFunctions.route()
    //             // 处理静态资源文件（JS、CSS、图片等）
    //             .GET("/assets/**", this::serveStaticResource)
    //             .GET("/favicon.svg", this::serveStaticResource)
    //             // 只处理根路径首页，其它路径交给 Gateway 路由
    //             .GET("/", this::serveIndex)
    //             .GET("/index.html", this::serveIndex)
    //             .build();
    // }

    /**
     * 提供静态资源
     */
    @SuppressWarnings("unused")
    private Mono<ServerResponse> serveStaticResource(ServerRequest request) {
        try {
            String path = request.uri().getPath();
            // 移除开头的斜杠
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            Resource resource = new ClassPathResource("static/" + path);

            if (!resource.exists()) {
                return ServerResponse.notFound().build();
            }

            // 根据文件扩展名设置 Content-Type
            String contentType = getContentType(path);

            return ServerResponse.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(BodyInserters.fromResource(resource));
        } catch (Exception e) {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 提供 index.html（用于 SPA 路由）
     */
    @SuppressWarnings("unused")
    private Mono<ServerResponse> serveIndex(ServerRequest request) {
        try {
            // 排除 API 路径和文档路径
            String path = request.uri().getPath();
            if (path.startsWith("/api") ||
                path.startsWith("/doc.html") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v2/api-docs") ||
                path.startsWith("/webjars")) {
                // 交给 Gateway 其他路由处理
                return ServerResponse.notFound().build();
            }

            Resource resource = new ClassPathResource("static/index.html");

            if (!resource.exists()) {
                return ServerResponse.notFound().build();
            }

            return ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(BodyInserters.fromResource(resource));
        } catch (Exception e) {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据文件扩展名获取 Content-Type
     */
    private String getContentType(String path) {
        if (path.endsWith(".js")) {
            return "application/javascript";
        } else if (path.endsWith(".css")) {
            return "text/css";
        } else if (path.endsWith(".html")) {
            return "text/html";
        } else if (path.endsWith(".json")) {
            return "application/json";
        } else if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (path.endsWith(".gif")) {
            return "image/gif";
        } else if (path.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (path.endsWith(".ico")) {
            return "image/x-icon";
        } else if (path.endsWith(".woff") || path.endsWith(".woff2")) {
            return "font/woff2";
        } else if (path.endsWith(".ttf")) {
            return "font/ttf";
        } else {
            return "application/octet-stream";
        }
    }
}

