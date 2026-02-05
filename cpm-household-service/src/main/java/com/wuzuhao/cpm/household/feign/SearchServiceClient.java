package com.wuzuhao.cpm.household.feign;

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
     * 搜索户籍信息
     */
    @GetMapping("/household")
    Result<Map<String, Object>> searchHousehold(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size);

    /**
     * 搜索户籍成员信息
     */
    @GetMapping("/household-member")
    Result<Map<String, Object>> searchHouseholdMember(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "householdId", required = false) Long householdId);
}
