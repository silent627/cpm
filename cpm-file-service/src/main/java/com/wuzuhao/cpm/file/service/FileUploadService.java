package com.wuzuhao.cpm.file.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    /**
     * 上传文件
     *
     * @param file 文件
     * @param subPath 子路径（如：avatar、idCard）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String subPath);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 替换文件（如果原文件存在则删除，然后上传新文件）
     *
     * @param file 新文件
     * @param subPath 子路径（如：avatar、idCard）
     * @param oldFileUrl 原文件URL（如果为null或空，则只上传新文件）
     * @return 新文件访问URL
     */
    String replaceFile(MultipartFile file, String subPath, String oldFileUrl);
}
