package com.wuzuhao.cpm.household.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 户籍实体类
 */
@Data
@TableName("household")
public class Household implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 户主ID（关联resident表）
     */
    private Long headId;

    /**
     * 户主姓名
     */
    private String headName;

    /**
     * 户主身份证号
     */
    private String headIdCard;

    /**
     * 户籍编号
     */
    private String householdNo;

    /**
     * 户籍地址
     */
    private String address;

    /**
     * 户别：1-家庭户, 2-集体户
     */
    private Integer householdType;

    /**
     * 户人数
     */
    private Integer memberCount;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 迁入日期
     */
    private LocalDateTime moveInDate;

    /**
     * 迁出日期
     */
    private LocalDateTime moveOutDate;

    /**
     * 迁入原因
     */
    private String moveInReason;

    /**
     * 迁出原因
     */
    private String moveOutReason;

    /**
     * 状态：0-迁出, 1-正常
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

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

