package com.wuzuhao.cpm.statistics.feign;

import com.wuzuhao.cpm.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 居民服务Feign客户端
 */
@FeignClient(name = "cpm-resident-service", path = "/resident")
public interface ResidentServiceClient {
    
    /**
     * 获取所有居民列表（用于统计）
     */
    @GetMapping("/all")
    Result<Object> getAllResidents();
}

