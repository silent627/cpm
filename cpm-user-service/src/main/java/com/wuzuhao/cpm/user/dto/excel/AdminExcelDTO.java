package com.wuzuhao.cpm.user.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 管理员导出Excel DTO
 */
@Data
@ColumnWidth(20)
public class AdminExcelDTO {

    @ExcelProperty(value = "ID", index = 0)
    private Long id;

    @ExcelProperty(value = "管理员编号", index = 1)
    private String adminNo;

    @ExcelProperty(value = "用户名", index = 2)
    private String username;

    @ExcelProperty(value = "真实姓名", index = 3)
    private String realName;

    @ExcelProperty(value = "手机号", index = 4)
    private String phone;

    @ExcelProperty(value = "邮箱", index = 5)
    private String email;

    @ExcelProperty(value = "部门", index = 6)
    private String department;

    @ExcelProperty(value = "职位", index = 7)
    private String position;

    @ExcelProperty(value = "备注", index = 8)
    @ColumnWidth(30)
    private String remark;

    @ExcelProperty(value = "创建时间", index = 9)
    @ColumnWidth(25)
    private String createTime;
}

