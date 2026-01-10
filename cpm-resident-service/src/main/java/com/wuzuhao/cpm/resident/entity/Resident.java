package com.wuzuhao.cpm.resident.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 居民实体类
 */
@Data
@TableName("resident")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resident implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联sys_user表）
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 性别：0-女, 1-男
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 民族
     */
    private String nationality;

    /**
     * 户籍地址
     */
    private String registeredAddress;

    /**
     * 现居住地址
     */
    private String currentAddress;

    /**
     * 职业
     */
    private String occupation;

    /**
     * 文化程度
     */
    private String education;

    /**
     * 婚姻状况：0-未婚, 1-已婚, 2-离异, 3-丧偶
     */
    private Integer maritalStatus;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系人电话
     */
    private String emergencyPhone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 头像路径
     */
    private String avatar;

    /**
     * 身份证照片路径
     */
    private String idCardPhoto;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除, 1-已删除
     */
    @TableLogic
    private Integer deleted;
}

