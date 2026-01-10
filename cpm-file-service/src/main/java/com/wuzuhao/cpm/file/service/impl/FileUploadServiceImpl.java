package com.wuzuhao.cpm.file.service.impl;

import com.wuzuhao.cpm.file.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传服务实现类
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;
    
    /**
     * 获取上传路径的绝对路径
     */
    private String getUploadAbsolutePath() {
        String path = uploadPath;
        // 如果是相对路径，转换为绝对路径
        if (!path.startsWith("/") && !path.contains(":")) {
            // 获取项目根目录
            String projectPath = System.getProperty("user.dir");
            if (projectPath == null || projectPath.isEmpty()) {
                projectPath = "/app";
            }
            path = projectPath + File.separator + path;
        }
        return path;
    }

    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp"};
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public String uploadFile(MultipartFile file, String subPath) {
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new RuntimeException("不支持的文件类型，仅支持：jpg、jpeg、png、gif、bmp");
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过10MB");
        }

        try {
            // 生成文件存储路径：uploads/subPath/yyyy-MM-dd/uuid.扩展名
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + extension;
            // 确保 subPath 不以 / 开头或结尾，避免路径问题
            String cleanedSubPath = subPath.replaceAll("^/|/$", "");
            String relativePath = cleanedSubPath + File.separator + datePath + File.separator + fileName;
            String basePath = getUploadAbsolutePath();
            String fullPath = basePath + File.separator + relativePath;

            // 创建目录
            Path directory = Paths.get(fullPath).getParent();
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 保存文件
            File targetFile = new File(fullPath);
            file.transferTo(targetFile);
            
            // 验证文件是否保存成功
            if (!targetFile.exists()) {
                throw new RuntimeException("文件保存失败，文件不存在");
            }
            
            // 返回访问URL
            String accessUrl = urlPrefix + "/" + relativePath.replace(File.separator, "/");
            log.info("文件上传成功: {}", fullPath);
            log.debug("文件访问URL: {}", accessUrl);
            return accessUrl;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        try {
            // 从URL中提取相对路径
            String relativePath = fileUrl.replace(urlPrefix + "/", "");
            String basePath = getUploadAbsolutePath();
            String fullPath = basePath + File.separator + relativePath.replace("/", File.separator);

            File file = new File(fullPath);
            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", fullPath);
                    // 递归删除空目录
                    deleteEmptyParentDirectories(file.getParentFile(), basePath);
                }
                return deleted;
            } else {
                log.warn("文件不存在: {}", fullPath);
            }
            return false;
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 递归删除空目录
     * @param directory 要检查的目录
     * @param basePath 基础路径，防止删除基础上传目录
     */
    private void deleteEmptyParentDirectories(File directory, String basePath) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }

        try {
            // 检查目录是否为空
            File[] files = directory.listFiles();
            if (files != null && files.length == 0) {
                // 获取目录的绝对路径
                String dirPath = directory.getAbsolutePath();
                String basePathNormalized = new File(basePath).getAbsolutePath();
                
                // 防止删除基础上传目录
                if (!dirPath.equals(basePathNormalized) && !dirPath.startsWith(basePathNormalized + File.separator)) {
                    return;
                }
                
                // 删除空目录
                boolean deleted = directory.delete();
                if (deleted) {
                    log.debug("空目录已删除: {}", dirPath);
                    // 递归检查父目录
                    File parentDir = directory.getParentFile();
                    if (parentDir != null) {
                        deleteEmptyParentDirectories(parentDir, basePath);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略目录删除失败，不影响主流程
            log.debug("删除空目录失败: {}", directory.getAbsolutePath(), e);
        }
    }

    @Override
    public String replaceFile(MultipartFile file, String subPath, String oldFileUrl) {
        // 如果原文件存在，先删除原文件
        if (oldFileUrl != null && !oldFileUrl.trim().isEmpty()) {
            try {
                deleteFile(oldFileUrl);
                log.info("原文件已删除: {}", oldFileUrl);
            } catch (Exception e) {
                // 删除失败不影响新文件上传，记录日志即可
                log.warn("删除原文件失败，继续上传新文件: {}", oldFileUrl, e);
            }
        }
        
        // 上传新文件
        return uploadFile(file, subPath);
    }

    /**
     * 检查是否为允许的图片类型
     */
    private boolean isAllowedImageType(String contentType) {
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
