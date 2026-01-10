package com.wuzuhao.cpm.resident.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 居民导入Excel DTO
 */
@Data
@ColumnWidth(20)
public class ResidentImportDTO {

    @ExcelProperty(value = "真实姓名", index = 0)
    private String realName;

    @ExcelProperty(value = "身份证号", index = 1)
    @ColumnWidth(25)
    private String idCard;

    @ExcelProperty(value = "性别", index = 2)
    private String gender;

    @ExcelProperty(value = "出生日期", index = 3)
    private String birthDate;

    @ExcelProperty(value = "民族", index = 4)
    private String nationality;

    @ExcelProperty(value = "户籍地址", index = 5)
    @ColumnWidth(30)
    private String registeredAddress;

    @ExcelProperty(value = "现居住地址", index = 6)
    @ColumnWidth(30)
    private String currentAddress;

    @ExcelProperty(value = "职业", index = 7)
    private String occupation;

    @ExcelProperty(value = "文化程度", index = 8)
    private String education;

    @ExcelProperty(value = "婚姻状况", index = 9)
    private String maritalStatus;

    @ExcelProperty(value = "联系电话", index = 10)
    private String contactPhone;

    @ExcelProperty(value = "紧急联系人", index = 11)
    private String emergencyContact;

    @ExcelProperty(value = "紧急联系人电话", index = 12)
    private String emergencyPhone;
}

