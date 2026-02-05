package com.wuzuhao.cpm.search.feign;

import com.wuzuhao.cpm.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "cpm-user-service", contextId = "userService", path = "/user")
public interface UserServiceClient {
    
    /**
     * 获取所有用户列表（用于索引同步）
     */
    @GetMapping("/all")
    Result<?> getAllUsers();
}
