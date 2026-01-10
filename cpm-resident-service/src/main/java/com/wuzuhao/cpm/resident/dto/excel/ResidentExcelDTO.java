package com.wuzuhao.cpm.resident.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 居民导出Excel DTO
 */
@Data
@ColumnWidth(20)
public class ResidentExcelDTO {

    @ExcelProperty(value = "ID", index = 0)
    private Long id;

    @ExcelProperty(value = "真实姓名", index = 1)
    private String realName;

    @ExcelProperty(value = "身份证号", index = 2)
    @ColumnWidth(25)
    private String idCard;

    @ExcelProperty(value = "性别", index = 3)
    private String gender;

    @ExcelProperty(value = "出生日期", index = 4)
    private String birthDate;

    @ExcelProperty(value = "民族", index = 5)
    private String nationality;

    @ExcelProperty(value = "户籍地址", index = 6)
    @ColumnWidth(30)
    private String registeredAddress;

    @ExcelProperty(value = "现居住地址", index = 7)
    @ColumnWidth(30)
    private String currentAddress;

    @ExcelProperty(value = "职业", index = 8)
    private String occupation;

    @ExcelProperty(value = "文化程度", index = 9)
    private String education;

    @ExcelProperty(value = "婚姻状况", index = 10)
    private String maritalStatus;

    @ExcelProperty(value = "联系电话", index = 11)
    private String contactPhone;

    @ExcelProperty(value = "紧急联系人", index = 12)
    private String emergencyContact;

    @ExcelProperty(value = "紧急联系人电话", index = 13)
    private String emergencyPhone;

    @ExcelProperty(value = "创建时间", index = 14)
    @ColumnWidth(25)
    private String createTime;
}

