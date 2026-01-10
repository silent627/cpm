package com.wuzuhao.cpm.household.feign;

import com.wuzuhao.cpm.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 居民服务Feign客户端
 */
@FeignClient(name = "cpm-resident-service", path = "/resident")
public interface ResidentServiceClient {
    
    /**
     * 根据ID获取居民
     */
    @GetMapping("/{id}")
    Result<Object> getResidentById(@PathVariable("id") Long id);
}

