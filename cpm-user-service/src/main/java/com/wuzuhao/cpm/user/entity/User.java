package com.wuzuhao.cpm.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@ApiModel(description = "用户实体")
@Data
@TableName("sys_user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名", required = true, example = "admin")
    private String username;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码", hidden = true)
    private String password;

    /**
     * 真实姓名
     */
    @ApiModelProperty(value = "真实姓名", example = "张三")
    private String realName;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱", example = "admin@example.com")
    private String email;

    /**
     * 头像路径
     */
    @ApiModelProperty(value = "头像路径")
    private String avatar;

    /**
     * 角色：USER-普通用户, ADMIN-管理员
     */
    @ApiModelProperty(value = "角色", example = "ADMIN", allowableValues = "USER,ADMIN")
    private String role;

    /**
     * 状态：0-禁用, 1-启用
     */
    @ApiModelProperty(value = "状态：0-禁用, 1-启用", example = "1")
    private Integer status;

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

