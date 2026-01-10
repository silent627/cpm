package com.wuzuhao.cpm.user.feign;

import com.wuzuhao.cpm.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 文件服务 Feign 客户端
 * 用于调用文件服务的文件上传和删除接口
 */
@FeignClient(name = "cpm-file-service", path = "/upload")
public interface FileServiceClient {

    /**
     * 删除文件
     * @param url 文件URL
     * @return 删除结果
     */
    @DeleteMapping("/file")
    Result<?> deleteFile(@RequestParam("url") String url);
    
    /**
     * 处理文件更新（如果新文件URL与旧文件URL不同，则删除旧文件）
     * @param oldUrl 旧文件URL
     * @param newUrl 新文件URL
     * @return 处理结果
     */
    @PostMapping("/handle-update")
    Result<Boolean> handleFileUpdate(
            @RequestParam(value = "oldUrl", required = false) String oldUrl,
            @RequestParam(value = "newUrl", required = false) String newUrl);
}
