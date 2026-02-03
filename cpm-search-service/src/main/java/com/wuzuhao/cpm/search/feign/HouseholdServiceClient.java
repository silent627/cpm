package com.wuzuhao.cpm.search.feign;

import com.wuzuhao.cpm.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 户籍服务Feign客户端
 */
@FeignClient(name = "cpm-household-service", contextId = "householdService", path = "/household")
public interface HouseholdServiceClient {
    
    /**
     * 获取所有户籍列表（用于索引同步）
     */
    @GetMapping("/all")
    Result<?> getAllHouseholds();
}
