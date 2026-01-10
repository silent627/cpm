package com.wuzuhao.cpm.user.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 用户导出Excel DTO
 */
@Data
@ColumnWidth(20)
public class UserExcelDTO {

    @ExcelProperty(value = "ID", index = 0)
    private Long id;

    @ExcelProperty(value = "用户名", index = 1)
    private String username;

    @ExcelProperty(value = "真实姓名", index = 2)
    private String realName;

    @ExcelProperty(value = "手机号", index = 3)
    private String phone;

    @ExcelProperty(value = "邮箱", index = 4)
    private String email;

    @ExcelProperty(value = "角色", index = 5)
    private String role;

    @ExcelProperty(value = "状态", index = 6)
    private String status;

    @ExcelProperty(value = "创建时间", index = 7)
    private String createTime;
}

