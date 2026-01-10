package com.wuzuhao.cpm.resident.feign;

import com.wuzuhao.cpm.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "cpm-user-service", path = "/user")
public interface UserServiceClient {
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    Result<Object> getUserById(@PathVariable("id") Long id);
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    Result<Object> register(@RequestBody Map<String, Object> user);
}

