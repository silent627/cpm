package com.wuzuhao.cpm.statistics.feign;

import com.wuzuhao.cpm.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 户籍服务Feign客户端
 */
@FeignClient(name = "cpm-household-service", path = "/household")
public interface HouseholdServiceClient {
    
    /**
     * 获取所有户籍列表（用于统计）
     */
    @GetMapping("/all")
    Result<Object> getAllHouseholds();
}

