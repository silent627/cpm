package com.wuzuhao.cpm.resident.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.resident.dto.excel.ResidentExcelDTO;
import com.wuzuhao.cpm.resident.dto.excel.ResidentImportDTO;
import com.wuzuhao.cpm.resident.entity.Resident;
import com.wuzuhao.cpm.resident.feign.UserServiceClient;
import com.wuzuhao.cpm.resident.service.impl.ResidentServiceImpl;
import com.wuzuhao.cpm.util.ExcelUtil;
import com.wuzuhao.cpm.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel导出服务（居民服务专用）
 */
@Service
public class ExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ResidentServiceImpl residentService;

    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * 导出居民列表
     */
    public List<ResidentExcelDTO> exportResidents(String realName, String idCard, String currentAddress) {
        LambdaQueryWrapper<Resident> wrapper = new LambdaQueryWrapper<>();
        if (realName != null && !realName.isEmpty()) {
            wrapper.like(Resident::getRealName, realName);
        }
        if (idCard != null && !idCard.isEmpty()) {
            wrapper.like(Resident::getIdCard, idCard);
        }
        if (currentAddress != null && !currentAddress.isEmpty()) {
            wrapper.like(Resident::getCurrentAddress, currentAddress);
        }
        wrapper.orderByDesc(Resident::getCreateTime);
        
        List<Resident> residents = residentService.list(wrapper);
        
        // 转换为Excel DTO
        return residents.stream().map(resident -> {
            ResidentExcelDTO dto = new ResidentExcelDTO();
            dto.setId(resident.getId());
            dto.setRealName(resident.getRealName());
            dto.setIdCard(resident.getIdCard());
            dto.setGender(resident.getGender() != null && resident.getGender() == 1 ? "男" : "女");
            dto.setBirthDate(ExcelUtil.formatDate(resident.getBirthDate()));
            dto.setNationality(resident.getNationality());
            dto.setRegisteredAddress(resident.getRegisteredAddress());
            dto.setCurrentAddress(resident.getCurrentAddress());
            dto.setOccupation(resident.getOccupation());
            dto.setEducation(resident.getEducation());
            
            // 婚姻状况
            String maritalStatus = "";
            if (resident.getMaritalStatus() != null) {
                switch (resident.getMaritalStatus()) {
                    case 0: maritalStatus = "未婚"; break;
                    case 1: maritalStatus = "已婚"; break;
                    case 2: maritalStatus = "离异"; break;
                    case 3: maritalStatus = "丧偶"; break;
                }
            }
            dto.setMaritalStatus(maritalStatus);
            
            dto.setContactPhone(resident.getContactPhone());
            dto.setEmergencyContact(resident.getEmergencyContact());
            dto.setEmergencyPhone(resident.getEmergencyPhone());
            dto.setCreateTime(ExcelUtil.formatDateTime(resident.getCreateTime()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 下载居民导入模板
     */
    public void downloadResidentTemplate(HttpServletResponse response) {
        try {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            
            String encodedFileName = URLEncoder.encode("居民信息导入模板", StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFileName + ".xlsx\"; filename*=UTF-8''" + encodedFileName + ".xlsx");

            // 创建一个空的模板数据列表
            List<ResidentImportDTO> templateData = new ArrayList<>();
            // 添加一行示例数据
            ResidentImportDTO example = new ResidentImportDTO();
            example.setRealName("张三");
            example.setIdCard("110101199001011234");
            example.setGender("男");
            example.setBirthDate("1990-01-01");
            example.setNationality("汉族");
            example.setRegisteredAddress("北京市东城区XX街道XX号");
            example.setCurrentAddress("北京市东城区XX街道XX号");
            example.setOccupation("工程师");
            example.setEducation("本科");
            example.setMaritalStatus("已婚");
            example.setContactPhone("13800138000");
            example.setEmergencyContact("李四");
            example.setEmergencyPhone("13900139000");
            templateData.add(example);
            
            EasyExcel.write(response.getOutputStream(), ResidentImportDTO.class)
                    .sheet("居民信息")
                    .doWrite(templateData);
        } catch (IOException e) {
            log.error("下载导入模板失败", e);
            throw new RuntimeException("下载导入模板失败", e);
        }
    }

    /**
     * 导入居民信息
     */
    public Map<String, Object> importResidents(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        int rowNum = 2; // Excel行号从2开始（第1行是表头）

        try {
            List<ResidentImportDTO> importData = new ArrayList<>();
            
            // 读取Excel文件
            EasyExcel.read(file.getInputStream(), ResidentImportDTO.class, new ReadListener<ResidentImportDTO>() {
                @Override
                public void invoke(ResidentImportDTO data, AnalysisContext context) {
                    importData.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 读取完成
                }
            }).sheet().doRead();

            // 处理导入数据
            for (ResidentImportDTO dto : importData) {
                rowNum++;
                try {
                    // 数据验证
                    String errorMsg = validateResidentImport(dto);
                    if (errorMsg != null) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("row", rowNum);
                        error.put("message", errorMsg);
                        errors.add(error);
                        failCount++;
                        continue;
                    }

                    // 检查身份证号是否已存在
                    Resident existResident = residentService.getByIdCard(dto.getIdCard());
                    if (existResident != null) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("row", rowNum);
                        error.put("message", "身份证号已存在：" + dto.getIdCard());
                        errors.add(error);
                        failCount++;
                        continue;
                    }

                    // 通过Feign调用用户服务创建用户
                    Map<String, Object> userParams = new HashMap<>();
                    userParams.put("username", dto.getIdCard()); // 使用身份证号作为用户名
                    userParams.put("password", "123456"); // 默认密码
                    userParams.put("realName", dto.getRealName());
                    userParams.put("role", "USER");
                    userParams.put("status", 1);
                    
                    Result<Object> userResult = userServiceClient.register(userParams);
                    if (userResult == null || userResult.getCode() != 200) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("row", rowNum);
                        error.put("message", "创建用户失败：" + (userResult != null ? userResult.getMessage() : "未知错误"));
                        errors.add(error);
                        failCount++;
                        continue;
                    }
                    
                    // 获取用户ID
                    Long userId = null;
                    if (userResult.getData() != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) userResult.getData();
                        if (userData.get("id") != null) {
                            userId = Long.valueOf(userData.get("id").toString());
                        }
                    }
                    
                    if (userId == null) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("row", rowNum);
                        error.put("message", "无法获取用户ID");
                        errors.add(error);
                        failCount++;
                        continue;
                    }

                    // 创建居民信息
                    Resident resident = new Resident();
                    resident.setUserId(userId);
                    resident.setRealName(dto.getRealName());
                    resident.setIdCard(dto.getIdCard());
                    resident.setGender("男".equals(dto.getGender()) || "1".equals(dto.getGender()) ? 1 : 0);
                    
                    // 解析出生日期
                    if (dto.getBirthDate() != null && !dto.getBirthDate().isEmpty()) {
                        try {
                            resident.setBirthDate(LocalDate.parse(dto.getBirthDate(), DATE_FORMATTER));
                        } catch (Exception e) {
                            resident.setBirthDate(null);
                        }
                    }
                    
                    resident.setNationality(dto.getNationality());
                    resident.setRegisteredAddress(dto.getRegisteredAddress());
                    resident.setCurrentAddress(dto.getCurrentAddress());
                    resident.setOccupation(dto.getOccupation());
                    resident.setEducation(dto.getEducation());
                    
                    // 解析婚姻状况
                    if (dto.getMaritalStatus() != null) {
                        switch (dto.getMaritalStatus()) {
                            case "未婚": resident.setMaritalStatus(0); break;
                            case "已婚": resident.setMaritalStatus(1); break;
                            case "离异": resident.setMaritalStatus(2); break;
                            case "丧偶": resident.setMaritalStatus(3); break;
                            default: resident.setMaritalStatus(0);
                        }
                    }
                    
                    resident.setContactPhone(dto.getContactPhone());
                    resident.setEmergencyContact(dto.getEmergencyContact());
                    resident.setEmergencyPhone(dto.getEmergencyPhone());

                    residentService.createResident(userId, resident);
                    successCount++;
                } catch (Exception e) {
                    log.error("导入居民信息失败，行号：{}", rowNum, e);
                    Map<String, Object> error = new HashMap<>();
                    error.put("row", rowNum);
                    error.put("message", "导入失败：" + e.getMessage());
                    errors.add(error);
                    failCount++;
                }
            }

            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("errors", errors);
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new RuntimeException("读取Excel文件失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 验证导入数据
     */
    private String validateResidentImport(ResidentImportDTO dto) {
        if (dto.getRealName() == null || dto.getRealName().trim().isEmpty()) {
            return "真实姓名不能为空";
        }
        if (dto.getIdCard() == null || dto.getIdCard().trim().isEmpty()) {
            return "身份证号不能为空";
        }
        // 验证身份证号格式（18位数字）
        if (!ValidationUtil.isValidIdCard(dto.getIdCard())) {
            return "身份证号格式不正确（应为18位数字）";
        }
        // 验证联系电话格式
        if (dto.getContactPhone() != null && !dto.getContactPhone().trim().isEmpty()) {
            if (!ValidationUtil.isValidPhone(dto.getContactPhone())) {
                return "联系电话格式不正确（应为11位数字且以1开头）";
            }
        }
        // 验证紧急联系人电话格式
        if (dto.getEmergencyPhone() != null && !dto.getEmergencyPhone().trim().isEmpty()) {
            if (!ValidationUtil.isValidPhone(dto.getEmergencyPhone())) {
                return "紧急联系人电话格式不正确（应为11位数字且以1开头）";
            }
        }
        return null;
    }
}

