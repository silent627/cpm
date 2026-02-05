package com.wuzuhao.cpm.resident.feign;

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
     * 搜索居民信息
     */
    @GetMapping("/resident")
    Result<Map<String, Object>> searchResident(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size);
}
