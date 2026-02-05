package com.wuzuhao.cpm.search.feign;

import com.wuzuhao.cpm.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 管理员服务Feign客户端
 */
@FeignClient(name = "cpm-user-service", contextId = "adminService", path = "/admin")
public interface AdminServiceClient {
    
    /**
     * 获取所有管理员列表（用于索引同步）
     */
    @GetMapping("/all")
    Result<?> getAllAdmins();
}
