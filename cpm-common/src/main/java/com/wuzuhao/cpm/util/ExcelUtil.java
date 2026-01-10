package com.wuzuhao.cpm.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel工具类
 */
public class ExcelUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 导出Excel
     *
     * @param response 响应对象
     * @param fileName 文件名（不含扩展名）
     * @param sheetName 工作表名称
     * @param data 数据列表
     * @param clazz Excel DTO类
     */
    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> data, Class<T> clazz) {
        try {
            // 设置响应头
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            
            // 文件名编码
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFileName + ".xlsx\"; filename*=UTF-8''" + encodedFileName + ".xlsx");

            // 写入Excel
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(sheetName)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 自动列宽
                    .doWrite(data);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    /**
     * 格式化日期时间
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * 格式化日期
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }
}

