package com.wuzuhao.cpm.household.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 户籍导出Excel DTO
 */
@Data
@ColumnWidth(20)
public class HouseholdExcelDTO {

    @ExcelProperty(value = "ID", index = 0)
    private Long id;

    @ExcelProperty(value = "户籍编号", index = 1)
    private String householdNo;

    @ExcelProperty(value = "户主姓名", index = 2)
    private String headName;

    @ExcelProperty(value = "户主身份证号", index = 3)
    @ColumnWidth(25)
    private String headIdCard;

    @ExcelProperty(value = "户籍地址", index = 4)
    @ColumnWidth(35)
    private String address;

    @ExcelProperty(value = "户别", index = 5)
    private String householdType;

    @ExcelProperty(value = "户人数", index = 6)
    private Integer memberCount;

    @ExcelProperty(value = "联系电话", index = 7)
    private String contactPhone;

    @ExcelProperty(value = "状态", index = 8)
    private String status;

    @ExcelProperty(value = "迁入日期", index = 9)
    @ColumnWidth(25)
    private String moveInDate;

    @ExcelProperty(value = "迁入原因", index = 10)
    @ColumnWidth(30)
    private String moveInReason;

    @ExcelProperty(value = "创建时间", index = 11)
    @ColumnWidth(25)
    private String createTime;
}

