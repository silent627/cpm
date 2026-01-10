package com.wuzuhao.cpm.household.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 户籍成员DTO（包含居民详细信息）
 */
@Data
public class HouseholdMemberDTO {
    private Long id;
    private Long householdId;
    private Long residentId;
    private String relationship;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;

    // 居民信息
    private String realName;
    private String idCard;
    private Integer gender;
    private LocalDate birthDate;
    private String nationality;
    private String currentAddress;
    private String occupation;
    private String education;
}

