package com.wuzuhao.cpm.search.feign;

import com.wuzuhao.cpm.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 户籍成员服务Feign客户端
 */
@FeignClient(name = "cpm-household-service", contextId = "householdMemberService", path = "/household-member")
public interface HouseholdMemberServiceClient {
    
    /**
     * 获取所有户籍成员列表（用于索引同步）
     */
    @GetMapping("/all")
    Result<?> getAllHouseholdMembers();
}
