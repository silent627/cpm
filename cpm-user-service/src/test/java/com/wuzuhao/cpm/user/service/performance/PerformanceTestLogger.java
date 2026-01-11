package com.wuzuhao.cpm.user.service.performance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 性能测试日志工具类
 * 负责将测试结果写入日志文件
 */
public class PerformanceTestLogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE_PREFIX = "performance-test-";
    private static final String LOG_FILE_SUFFIX = ".log";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
    
    private static String currentLogFile = null;
    private static BufferedWriter writer = null;

    /**
     * 获取或创建日志文件
     * 如果日志文件还未创建，则创建新的日志文件
     */
    private static synchronized BufferedWriter getLogWriter() throws IOException {
        if (writer == null) {
            // 创建logs目录
            File logDir = new File(LOG_DIR);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            // 生成日志文件名（包含时间戳）
            String timestamp = DATE_FORMAT.format(new Date());
            currentLogFile = LOG_DIR + File.separator + LOG_FILE_PREFIX + timestamp + LOG_FILE_SUFFIX;

            // 创建BufferedWriter（追加模式）
            writer = new BufferedWriter(new FileWriter(currentLogFile, true));
            
            // 写入文件头信息
            writer.write("\n==========================================");
            writer.newLine();
            writer.write("性能测试日志 - " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            writer.newLine();
            writer.write("==========================================");
            writer.newLine();
            writer.flush();
        }
        return writer;
    }

    /**
     * 写入日志内容
     */
    public static synchronized void writeLog(String content) {
        try {
            BufferedWriter logWriter = getLogWriter();
            logWriter.write(content);
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            System.err.println("写入日志文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 写入格式化日志（使用String.format）
     */
    public static synchronized void writeLog(String format, Object... args) {
        try {
            String content = String.format(format, args);
            BufferedWriter logWriter = getLogWriter();
            logWriter.write(content);
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            System.err.println("写入日志文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 关闭日志文件
     */
    public static synchronized void close() {
        if (writer != null) {
            try {
                writer.close();
                writer = null;
                System.out.println("\n性能测试日志已保存到: " + currentLogFile);
            } catch (IOException e) {
                System.err.println("关闭日志文件失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取当前日志文件路径
     */
    public static String getCurrentLogFile() {
        return currentLogFile;
    }
}
