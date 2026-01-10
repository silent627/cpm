package com.wuzuhao.cpm.file.service.impl;

import com.wuzuhao.cpm.file.service.FileManageService;
import com.wuzuhao.cpm.file.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文件管理服务实现类
 */
@Service
public class FileManageServiceImpl implements FileManageService {
    
    private static final Logger log = LoggerFactory.getLogger(FileManageServiceImpl.class);
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @Override
    public int batchDeleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return 0;
        }
        
        int successCount = 0;
        for (String fileUrl : fileUrls) {
            if (fileUrl != null && !fileUrl.trim().isEmpty()) {
                try {
                    if (fileUploadService.deleteFile(fileUrl)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("批量删除文件失败: {}", fileUrl, e);
                }
            }
        }
        return successCount;
    }
    
    @Override
    public boolean handleFileUpdate(String oldFileUrl, String newFileUrl) {
        // 如果旧文件URL为空或与新文件URL相同，则不需要删除
        if (oldFileUrl == null || oldFileUrl.trim().isEmpty()) {
            return false;
        }
        
        // 如果新文件URL为空或与旧文件URL相同，则不需要删除
        if (newFileUrl == null || newFileUrl.trim().isEmpty() || newFileUrl.equals(oldFileUrl)) {
            return false;
        }
        
        // 删除旧文件
        try {
            boolean deleted = fileUploadService.deleteFile(oldFileUrl);
            if (deleted) {
                log.info("文件更新时已删除旧文件: {}", oldFileUrl);
            }
            return deleted;
        } catch (Exception e) {
            log.warn("文件更新时删除旧文件失败: {}", oldFileUrl, e);
            return false;
        }
    }
    
    @Override
    public int handleMultipleFileUpdate(String[] oldFileUrls, String[] newFileUrls) {
        if (oldFileUrls == null || newFileUrls == null) {
            return 0;
        }
        
        int maxLength = Math.max(oldFileUrls.length, newFileUrls.length);
        int deletedCount = 0;
        
        for (int i = 0; i < maxLength; i++) {
            String oldFileUrl = (i < oldFileUrls.length) ? oldFileUrls[i] : null;
            String newFileUrl = (i < newFileUrls.length) ? newFileUrls[i] : null;
            
            if (handleFileUpdate(oldFileUrl, newFileUrl)) {
                deletedCount++;
            }
        }
        
        return deletedCount;
    }
}
