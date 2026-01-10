package com.wuzuhao.cpm.file.service;

import java.util.List;

/**
 * 文件管理服务接口
 * 提供高级文件管理功能，如批量删除、文件更新处理等
 */
public interface FileManageService {
    
    /**
     * 批量删除文件
     * @param fileUrls 文件URL列表
     * @return 成功删除的文件数量
     */
    int batchDeleteFiles(List<String> fileUrls);
    
    /**
     * 处理文件更新（如果新文件URL与旧文件URL不同，则删除旧文件）
     * @param oldFileUrl 旧文件URL
     * @param newFileUrl 新文件URL
     * @return 是否删除了旧文件
     */
    boolean handleFileUpdate(String oldFileUrl, String newFileUrl);
    
    /**
     * 处理多个文件字段的更新
     * @param oldFileUrls 旧文件URL数组
     * @param newFileUrls 新文件URL数组
     * @return 成功删除的文件数量
     */
    int handleMultipleFileUpdate(String[] oldFileUrls, String[] newFileUrls);
}
