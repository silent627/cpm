package com.wuzuhao.cpm.resident.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.resident.dto.excel.ResidentExcelDTO;
import com.wuzuhao.cpm.resident.entity.Resident;
import com.wuzuhao.cpm.resident.feign.SearchServiceClient;
import com.wuzuhao.cpm.resident.feign.UserServiceClient;
import com.wuzuhao.cpm.resident.service.ExcelExportService;
import com.wuzuhao.cpm.resident.service.ResidentService;
import com.wuzuhao.cpm.util.ExcelUtil;
import com.wuzuhao.cpm.util.ValidationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 居民控制器
 */
@Slf4j
@Api(tags = "居民管理")
@RestController
@RequestMapping("/resident")
public class ResidentController {

    @Autowired
    private ResidentService residentService;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private SearchServiceClient searchServiceClient;

    @Autowired
    private ExcelExportService excelExportService;

    /**
     * 获取当前居民信息
     */
    @ApiOperation(value = "获取当前居民信息", notes = "获取当前登录居民的信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getCurrentResident(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Resident resident = residentService.getByUserId(userId);
        Result<Object> userResult = userServiceClient.getUserById(userId);
        Object user = userResult != null && userResult.getCode() == 200 ? userResult.getData() : null;

        Map<String, Object> result = new HashMap<>();
        result.put("resident", resident);
        result.put("user", user);
        return Result.success(result);
    }

    /**
     * 创建居民
     */
    @ApiOperation(value = "创建居民", notes = "创建新的居民账号和信息")
    @PostMapping("/create")
    public Result<Resident> createResident(@RequestBody Map<String, Object> params) {
        // 通过Feign调用用户服务创建用户
        Map<String, Object> userParams = new HashMap<>();
        userParams.put("username", params.get("username"));
        userParams.put("password", params.get("password"));
        userParams.put("realName", params.get("realName"));
        userParams.put("phone", params.get("phone"));
        userParams.put("email", params.get("email"));
        userParams.put("role", "USER");
        
        Result<Object> userResult = userServiceClient.register(userParams);
        if (userResult == null || userResult.getCode() != 200) {
            return Result.error(userResult != null ? userResult.getMessage() : "创建用户失败");
        }
        
        // 从返回结果中获取用户ID（需要根据实际返回结构调整）
        Long userId = null;
        if (userResult.getData() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userData = (Map<String, Object>) userResult.getData();
            if (userData.get("id") != null) {
                userId = Long.valueOf(userData.get("id").toString());
            }
        }
        
        if (userId == null) {
            return Result.error("无法获取用户ID");
        }

        // 创建居民信息
        Resident resident = new Resident();
        resident.setRealName((String) params.get("realName"));
        resident.setIdCard((String) params.get("idCard"));
        if (params.get("gender") != null) {
            resident.setGender(Integer.valueOf(params.get("gender").toString()));
        }
        if (params.get("birthDate") != null) {
            resident.setBirthDate(java.time.LocalDate.parse(params.get("birthDate").toString()));
        }
        resident.setNationality((String) params.get("nationality"));
        resident.setRegisteredAddress((String) params.get("registeredAddress"));
        resident.setCurrentAddress((String) params.get("currentAddress"));
        resident.setOccupation((String) params.get("occupation"));
        resident.setEducation((String) params.get("education"));
        if (params.get("maritalStatus") != null) {
            resident.setMaritalStatus(Integer.valueOf(params.get("maritalStatus").toString()));
        }
        resident.setContactPhone((String) params.get("contactPhone"));
        resident.setEmergencyContact((String) params.get("emergencyContact"));
        resident.setEmergencyPhone((String) params.get("emergencyPhone"));
        resident.setRemark((String) params.get("remark"));
        resident = residentService.createResident(userId, resident);

        return Result.success("创建成功", resident);
    }

    /**
     * 更新居民信息（根据ID）
     */
    @ApiOperation(value = "更新居民信息", notes = "根据居民ID更新居民信息")
    @PutMapping("/update/{id}")
    public Result<?> updateResident(@ApiParam(value = "居民ID", required = true) @PathVariable Long id, @RequestBody Resident resident) {
        Resident existResident = residentService.getById(id);
        if (existResident == null) {
            return Result.error("居民信息不存在");
        }

        // 如果更新了身份证号，需要验证格式和唯一性
        if (resident.getIdCard() != null && !resident.getIdCard().equals(existResident.getIdCard())) {
            if (!ValidationUtil.isValidIdCard(resident.getIdCard())) {
                return Result.error("身份证号格式不正确，应为18位数字");
            }
            Resident existByIdCard = residentService.getByIdCard(resident.getIdCard());
            if (existByIdCard != null && !existByIdCard.getId().equals(id)) {
                return Result.error("身份证号已被其他居民使用");
            }
        }

        // 验证联系电话格式
        if (resident.getContactPhone() != null && !resident.getContactPhone().trim().isEmpty()) {
            if (!ValidationUtil.isValidPhone(resident.getContactPhone())) {
                return Result.error("联系电话格式不正确，应为11位数字且以1开头");
            }
        }

        // 验证紧急联系人电话格式
        if (resident.getEmergencyPhone() != null && !resident.getEmergencyPhone().trim().isEmpty()) {
            if (!ValidationUtil.isValidPhone(resident.getEmergencyPhone())) {
                return Result.error("紧急联系人电话格式不正确，应为11位数字且以1开头");
            }
        }

        resident.setId(id);
        resident.setUserId(existResident.getUserId());
        // 清除时间字段，让 MyBatis-Plus 自动填充
        resident.setCreateTime(null);
        resident.setUpdateTime(null);
        residentService.updateById(resident);
        return Result.success("更新成功");
    }

    /**
     * 更新当前登录居民信息
     */
    @ApiOperation(value = "更新当前居民信息", notes = "更新当前登录居民的信息")
    @PutMapping("/update")
    public Result<?> updateCurrentResident(@RequestBody Resident resident, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Resident existResident = residentService.getByUserId(userId);
        if (existResident != null) {
            resident.setId(existResident.getId());
            resident.setUserId(userId);
            // 清除时间字段，让 MyBatis-Plus 自动填充
            resident.setCreateTime(null);
            resident.setUpdateTime(null);
            residentService.updateById(resident);
            return Result.success("更新成功");
        }
        return Result.error("居民信息不存在");
    }

    /**
     * 分页查询居民列表（使用Elasticsearch全文检索）
     */
    @ApiOperation(value = "分页查询居民列表", notes = "使用Elasticsearch全文检索查询居民信息")
    @GetMapping("/list")
    public Result<Page<Resident>> getResidentList(
            @ApiParam(value = "当前页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "真实姓名（模糊查询）") @RequestParam(required = false) String realName,
            @ApiParam(value = "身份证号（模糊查询）") @RequestParam(required = false) String idCard,
            @ApiParam(value = "现居住地址（模糊查询）") @RequestParam(required = false) String currentAddress) {
        try {
            // 合并查询参数为keyword
            StringBuilder keywordBuilder = new StringBuilder();
            if (realName != null && !realName.trim().isEmpty()) {
                keywordBuilder.append(realName.trim());
            }
            if (idCard != null && !idCard.trim().isEmpty()) {
                if (keywordBuilder.length() > 0) {
                    keywordBuilder.append(" ");
                }
                keywordBuilder.append(idCard.trim());
            }
            if (currentAddress != null && !currentAddress.trim().isEmpty()) {
                if (keywordBuilder.length() > 0) {
                    keywordBuilder.append(" ");
                }
                keywordBuilder.append(currentAddress.trim());
            }
            
            String keyword = keywordBuilder.length() > 0 ? keywordBuilder.toString() : "*";
            
            // 调用搜索服务（page参数从1开始，Elasticsearch从0开始，需要减1）
            Result<Map<String, Object>> searchResult = searchServiceClient.searchResident(keyword, current - 1, size);
            
            if (searchResult == null || searchResult.getCode() != 200 || searchResult.getData() == null) {
                // 如果搜索服务失败，返回空结果
                Page<Resident> page = new Page<>(current, size);
                page.setTotal(0);
                return Result.success(page);
            }
            
            Map<String, Object> data = searchResult.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> hits = (List<Map<String, Object>>) data.get("hits");
            Long total = ((Number) data.get("total")).longValue();
            
            // 转换为Resident实体列表
            List<Resident> residents = new ArrayList<>();
            if (hits != null) {
                for (Map<String, Object> hit : hits) {
                    try {
                        Resident resident = convertMapToResident(hit);
                        residents.add(resident);
                    } catch (Exception e) {
                        // 忽略转换失败的数据
                        continue;
                    }
                }
            }
            
            // 构建Page对象
            Page<Resident> page = new Page<>(current, size);
            page.setTotal(total);
            page.setRecords(residents);
            return Result.success(page);
            
        } catch (Exception e) {
            log.error("搜索居民信息失败", e);
            // 如果搜索服务异常，返回空结果
            Page<Resident> page = new Page<>(current, size);
            page.setTotal(0);
            return Result.success(page);
        }
    }
    
    /**
     * 将Map转换为Resident实体
     */
    private Resident convertMapToResident(Map<String, Object> map) {
        Resident resident = new Resident();
        if (map.get("id") != null) {
            resident.setId(Long.valueOf(map.get("id").toString()));
        }
        if (map.get("userId") != null) {
            resident.setUserId(Long.valueOf(map.get("userId").toString()));
        }
        resident.setRealName((String) map.get("realName"));
        resident.setIdCard((String) map.get("idCard"));
        if (map.get("gender") != null) {
            resident.setGender(Integer.valueOf(map.get("gender").toString()));
        }
        // 日期格式：birthDate 使用 "yyyy-MM-dd"，createTime/updateTime 使用 "yyyy-MM-dd HH:mm:ss"
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        java.time.format.DateTimeFormatter dateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        if (map.get("birthDate") != null) {
            try {
                String birthDateStr = map.get("birthDate").toString();
                resident.setBirthDate(java.time.LocalDate.parse(birthDateStr, dateFormatter));
                log.debug("成功解析居民 birthDate: {}", birthDateStr);
            } catch (Exception e) {
                log.warn("解析居民 birthDate 失败: {}, 错误: {}", map.get("birthDate"), e.getMessage());
                // 忽略日期解析错误，继续处理其他字段
            }
        }
        resident.setNationality((String) map.get("nationality"));
        resident.setRegisteredAddress((String) map.get("registeredAddress"));
        resident.setCurrentAddress((String) map.get("currentAddress"));
        resident.setOccupation((String) map.get("occupation"));
        resident.setEducation((String) map.get("education"));
        if (map.get("maritalStatus") != null) {
            resident.setMaritalStatus(Integer.valueOf(map.get("maritalStatus").toString()));
        }
        resident.setContactPhone((String) map.get("contactPhone"));
        resident.setEmergencyContact((String) map.get("emergencyContact"));
        resident.setEmergencyPhone((String) map.get("emergencyPhone"));
        resident.setRemark((String) map.get("remark"));
        resident.setAvatar((String) map.get("avatar"));
        resident.setIdCardPhoto((String) map.get("idCardPhoto"));
        if (map.get("createTime") != null) {
            try {
                String createTimeStr = map.get("createTime").toString();
                resident.setCreateTime(java.time.LocalDateTime.parse(createTimeStr, dateTimeFormatter));
                log.debug("成功解析居民 createTime: {}", createTimeStr);
            } catch (Exception e) {
                log.warn("解析居民 createTime 失败: {}, 错误: {}", map.get("createTime"), e.getMessage());
                // 忽略日期解析错误，继续处理其他字段
            }
        }
        if (map.get("updateTime") != null) {
            try {
                String updateTimeStr = map.get("updateTime").toString();
                resident.setUpdateTime(java.time.LocalDateTime.parse(updateTimeStr, dateTimeFormatter));
                log.debug("成功解析居民 updateTime: {}", updateTimeStr);
            } catch (Exception e) {
                log.warn("解析居民 updateTime 失败: {}, 错误: {}", map.get("updateTime"), e.getMessage());
                // 忽略日期解析错误，继续处理其他字段
            }
        }
        return resident;
    }

    /**
     * 根据ID获取居民
     */
    @ApiOperation(value = "根据ID获取居民", notes = "根据居民ID获取详细信息")
    @GetMapping("/{id}")
    public Result<Resident> getResidentById(@ApiParam(value = "居民ID", required = true) @PathVariable Long id) {
        Resident resident = residentService.getById(id);
        return Result.success(resident);
    }

    /**
     * 获取所有居民列表（用于统计服务）
     */
    @ApiOperation(value = "获取所有居民列表", notes = "获取所有居民信息，用于统计服务")
    @GetMapping("/all")
    public Result<List<Resident>> getAllResidents() {
        List<Resident> residents = residentService.list();
        return Result.success(residents);
    }

    /**
     * 根据身份证号查询居民
     */
    @ApiOperation(value = "根据身份证号查询居民", notes = "根据身份证号查询居民信息")
    @GetMapping("/idCard/{idCard}")
    public Result<Resident> getResidentByIdCard(@ApiParam(value = "身份证号", required = true) @PathVariable String idCard) {
        Resident resident = residentService.getByIdCard(idCard);
        return Result.success(resident);
    }

    /**
     * 删除居民（逻辑删除）
     */
    @ApiOperation(value = "删除居民", notes = "逻辑删除居民信息")
    @DeleteMapping("/{id}")
    public Result<?> deleteResident(@ApiParam(value = "居民ID", required = true) @PathVariable Long id) {
        Resident resident = residentService.getById(id);
        if (resident == null) {
            return Result.error("居民信息不存在");
        }
        residentService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 导出居民列表（Excel）
     */
    @ApiOperation(value = "导出居民列表", notes = "导出居民列表为Excel文件，支持筛选条件")
    @GetMapping("/export")
    public void exportResidents(
            @ApiParam(value = "真实姓名（模糊查询）") @RequestParam(required = false) String realName,
            @ApiParam(value = "身份证号（模糊查询）") @RequestParam(required = false) String idCard,
            @ApiParam(value = "现居住地址（模糊查询）") @RequestParam(required = false) String currentAddress,
            HttpServletResponse response) {
        List<ResidentExcelDTO> data = excelExportService.exportResidents(realName, idCard, currentAddress);
        ExcelUtil.export(response, "居民列表", "居民列表", data, ResidentExcelDTO.class);
    }

    /**
     * 批量删除居民
     */
    @ApiOperation(value = "批量删除居民", notes = "批量删除居民信息")
    @PostMapping("/batch/delete")
    public Result<Integer> batchDeleteResidents(@RequestBody Map<String, List<Long>> params) {
        List<Long> ids = params.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的居民");
        }
        boolean success = residentService.removeByIds(ids);
        int count = success ? ids.size() : 0;
        return Result.success("批量删除成功", count);
    }

    /**
     * 下载导入模板
     */
    @ApiOperation(value = "下载导入模板", notes = "下载居民信息导入Excel模板")
    @GetMapping("/import/template")
    public void downloadTemplate(HttpServletResponse response) {
        excelExportService.downloadResidentTemplate(response);
    }

    /**
     * 导入居民信息
     */
    @ApiOperation(value = "导入居民信息", notes = "从Excel文件批量导入居民信息")
    @PostMapping("/import")
    public Result<Map<String, Object>> importResidents(
            @ApiParam(value = "Excel文件", required = true) @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = excelExportService.importResidents(file);
        return Result.success("导入完成", result);
    }
}

