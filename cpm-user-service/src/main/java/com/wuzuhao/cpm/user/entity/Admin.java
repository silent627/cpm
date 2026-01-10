package com.wuzuhao.cpm.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统管理员实体类
 */
@Data
@TableName("sys_admin")
public class Admin implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联sys_user表）
     */
    private Long userId;

    /**
     * 管理员编号
     */
    private String adminNo;

    /**
     * 部门
     */
    private String department;

    /**
     * 职位
     */
    private String position;

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

