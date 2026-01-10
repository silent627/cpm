package com.wuzuhao.cpm.household.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 户籍成员关系实体类
 */
@Data
@TableName("household_member")
public class HouseholdMember implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 户籍ID（关联household表）
     */
    private Long householdId;

    /**
     * 居民ID（关联resident表）
     */
    private Long residentId;

    /**
     * 与户主关系：户主、配偶、子女、父母、其他
     */
    private String relationship;

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

