package com.wuzuhao.cpm.user.feign;

import com.wuzuhao.cpm.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 搜索服务Feign客户端
 */
@FeignClient(name = "cpm-search-service", path = "/search")
public interface SearchServiceClient {
    
    /**
     * 搜索用户信息
     */
    @GetMapping("/user")
    Result<Map<String, Object>> searchUser(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size);

    /**
     * 搜索管理员信息
     */
    @GetMapping("/admin")
    Result<Map<String, Object>> searchAdmin(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size);
}
