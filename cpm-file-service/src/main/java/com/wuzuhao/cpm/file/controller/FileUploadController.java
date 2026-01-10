package com.wuzuhao.cpm.file.controller;

import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.file.service.FileManageService;
import com.wuzuhao.cpm.file.service.FileUploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Api(tags = "文件上传管理")
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private FileManageService fileManageService;

    /**
     * 上传头像
     */
    @ApiOperation(value = "上传头像", notes = "上传头像图片，支持jpg、jpeg、png、gif、bmp格式，最大10MB")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @ApiParam(value = "头像文件", required = true) @RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadFile(file, "avatar");
        return Result.success("上传成功", url);
    }

    /**
     * 上传身份证照片
     */
    @ApiOperation(value = "上传身份证照片", notes = "上传身份证照片，支持jpg、jpeg、png、gif、bmp格式，最大10MB")
    @PostMapping("/idCard")
    public Result<String> uploadIdCard(
            @ApiParam(value = "身份证照片文件", required = true) @RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadFile(file, "idCard");
        return Result.success("上传成功", url);
    }

    /**
     * 通用文件上传
     */
    @ApiOperation(value = "通用文件上传", notes = "通用文件上传接口，支持jpg、jpeg、png、gif、bmp格式，最大10MB")
    @PostMapping("/file")
    public Result<String> uploadFile(
            @ApiParam(value = "文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "子路径", example = "documents") @RequestParam(value = "subPath", defaultValue = "files") String subPath) {
        String url = fileUploadService.uploadFile(file, subPath);
        return Result.success("上传成功", url);
    }

    /**
     * 删除文件
     */
    @ApiOperation(value = "删除文件", notes = "根据文件URL删除文件")
    @DeleteMapping("/file")
    public Result<?> deleteFile(
            @ApiParam(value = "文件URL", required = true) @RequestParam("url") String url) {
        boolean success = fileUploadService.deleteFile(url);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
    
    /**
     * 批量删除文件
     */
    @ApiOperation(value = "批量删除文件", notes = "根据文件URL列表批量删除文件")
    @PostMapping("/batch/delete")
    public Result<Integer> batchDeleteFiles(
            @ApiParam(value = "文件URL列表", required = true) @RequestBody Map<String, List<String>> params) {
        List<String> fileUrls = params.get("urls");
        if (fileUrls == null || fileUrls.isEmpty()) {
            return Result.error("文件URL列表不能为空");
        }
        int deletedCount = fileManageService.batchDeleteFiles(fileUrls);
        return Result.success("批量删除完成", deletedCount);
    }
    
    /**
     * 处理文件更新
     */
    @ApiOperation(value = "处理文件更新", notes = "如果新文件URL与旧文件URL不同，则删除旧文件")
    @PostMapping("/handle-update")
    public Result<Boolean> handleFileUpdate(
            @ApiParam(value = "旧文件URL") @RequestParam(value = "oldUrl", required = false) String oldFileUrl,
            @ApiParam(value = "新文件URL") @RequestParam(value = "newUrl", required = false) String newFileUrl) {
        boolean deleted = fileManageService.handleFileUpdate(oldFileUrl, newFileUrl);
        return Result.success(deleted ? "已删除旧文件" : "无需删除", deleted);
    }
}
