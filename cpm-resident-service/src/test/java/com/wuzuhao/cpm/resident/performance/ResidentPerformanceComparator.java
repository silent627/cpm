package com.wuzuhao.cpm.resident.performance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 居民搜索性能对比工具类
 * 用于对比 Elasticsearch 和 MyBatis-Plus 的性能差异
 */
public class ResidentPerformanceComparator {

    /**
     * 性能测试结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceResult {
        private String testName;
        private long totalTime; // 总时间（纳秒）
        private double averageTime; // 平均时间（毫秒）
        private double minTime; // 最小时间（毫秒）
        private double maxTime; // 最大时间（毫秒）
        private int totalRequests; // 总请求数
        private double qps; // 每秒查询数
        private long fastResponses; // 快速响应数（小于100ms认为是毫秒级响应）
        private double fastResponseRate; // 快速响应率
        private long millisecondResponses; // 毫秒级响应数（<100ms）
        private double millisecondResponseRate; // 毫秒级响应率
    }

    /**
     * 执行性能测试
     *
     * @param testName 测试名称
     * @param iterations 迭代次数
     * @param supplier 执行函数
     * @return 性能测试结果
     */
    public static PerformanceResult executePerformanceTest(String testName, int iterations, Supplier<Long> supplier) {
        List<Long> times = new ArrayList<>();
        long fastResponses = 0; // <10ms
        long millisecondResponses = 0; // <100ms

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            long singleStart = System.nanoTime();
            supplier.get(); // 执行测试操作
            long singleEnd = System.nanoTime();
            long duration = singleEnd - singleStart;
            times.add(duration);

            // 根据执行时间判断是否快速响应
            if (duration < 10_000_000) { // 10毫秒 = 10_000_000 纳秒
                fastResponses++;
            }
            if (duration < 100_000_000) { // 100毫秒 = 100_000_000 纳秒
                millisecondResponses++;
            }
        }
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;

        // 计算统计数据（使用double类型，保留两位小数）
        double totalMs = times.stream().mapToDouble(time -> time / 1_000_000.0).sum();
        double avgMs = totalMs / iterations;
        double minMs = times.stream().mapToDouble(time -> time / 1_000_000.0).min().orElse(0.0);
        double maxMs = times.stream().mapToDouble(time -> time / 1_000_000.0).max().orElse(0.0);
        double qps = (iterations * 1_000_000_000.0) / totalTime;
        double fastResponseRate = iterations > 0 ? (fastResponses * 100.0 / iterations) : 0;
        double millisecondResponseRate = iterations > 0 ? (millisecondResponses * 100.0 / iterations) : 0;

        return new PerformanceResult(testName, totalTime, avgMs, minMs, maxMs, iterations, qps, 
            fastResponses, fastResponseRate, millisecondResponses, millisecondResponseRate);
    }

    /**
     * 执行高并发性能测试
     *
     * @param testName 测试名称
     * @param threadCount 线程数
     * @param requestsPerThread 每个线程的请求数
     * @param supplier 执行函数
     * @return 性能测试结果
     */
    public static PerformanceResult executeConcurrentPerformanceTest(
            String testName, int threadCount, int requestsPerThread, Supplier<Long> supplier) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Long> times = new ArrayList<>();
        java.util.concurrent.atomic.AtomicLong fastResponses = new java.util.concurrent.atomic.AtomicLong(0);
        java.util.concurrent.atomic.AtomicLong millisecondResponses = new java.util.concurrent.atomic.AtomicLong(0);
        final Object lock = new Object();

        long startTime = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long singleStart = System.nanoTime();
                        supplier.get(); // 执行测试操作
                        long singleEnd = System.nanoTime();
                        long duration = singleEnd - singleStart;

                        synchronized (lock) {
                            times.add(duration);
                            // 根据执行时间判断是否快速响应
                            if (duration < 10_000_000) { // 10毫秒
                                fastResponses.incrementAndGet();
                            }
                            if (duration < 100_000_000) { // 100毫秒
                                millisecondResponses.incrementAndGet();
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(5, TimeUnit.MINUTES); // 最多等待5分钟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor.shutdown();

        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        int totalRequests = threadCount * requestsPerThread;

        // 计算统计数据
        double totalMs = times.stream().mapToDouble(time -> time / 1_000_000.0).sum();
        double avgMs = totalMs / totalRequests;
        double minMs = times.stream().mapToDouble(time -> time / 1_000_000.0).min().orElse(0.0);
        double maxMs = times.stream().mapToDouble(time -> time / 1_000_000.0).max().orElse(0.0);
        double qps = (totalRequests * 1_000_000_000.0) / totalTime;
        double fastResponseRate = totalRequests > 0 ? (fastResponses.get() * 100.0 / totalRequests) : 0;
        double millisecondResponseRate = totalRequests > 0 ? (millisecondResponses.get() * 100.0 / totalRequests) : 0;

        return new PerformanceResult(testName, totalTime, avgMs, minMs, maxMs, totalRequests, qps, 
            fastResponses.get(), fastResponseRate, millisecondResponses.get(), millisecondResponseRate);
    }

    /**
     * 打印性能对比结果
     */
    public static void printComparison(PerformanceResult mybatisResult, PerformanceResult elasticsearchResult, String operation) {
        StringBuilder logContent = new StringBuilder();
        
        // 构建输出内容
        logContent.append("\n==========================================").append("\n");
        logContent.append("性能对比测试: ").append(operation).append("\n");
        logContent.append("==========================================").append("\n");
        logContent.append(String.format("%-30s | %-25s | %-25s%n", "指标", "MyBatis-Plus", "Elasticsearch"));
        logContent.append("------------------------------------------").append("\n");
        logContent.append(String.format("%-30s | %-25s | %-25s%n", "测试名称", mybatisResult.getTestName(), elasticsearchResult.getTestName()));
        logContent.append(String.format("%-30s | %-25d | %-25d%n", "总请求数", mybatisResult.getTotalRequests(), elasticsearchResult.getTotalRequests()));
        logContent.append(String.format("%-30s | %-25.2f | %-25.2f%n", "平均响应时间(ms)", mybatisResult.getAverageTime(), elasticsearchResult.getAverageTime()));
        logContent.append(String.format("%-30s | %-25.2f | %-25.2f%n", "最小响应时间(ms)", mybatisResult.getMinTime(), elasticsearchResult.getMinTime()));
        logContent.append(String.format("%-30s | %-25.2f | %-25.2f%n", "最大响应时间(ms)", mybatisResult.getMaxTime(), elasticsearchResult.getMaxTime()));
        logContent.append(String.format("%-30s | %-25.2f | %-25.2f%n", "QPS (每秒查询数)", mybatisResult.getQps(), elasticsearchResult.getQps()));
        logContent.append(String.format("%-30s | %-25.2f%% | %-25.2f%%%n", "快速响应率(<10ms)", mybatisResult.getFastResponseRate(), elasticsearchResult.getFastResponseRate()));
        logContent.append(String.format("%-30s | %-25.2f%% | %-25.2f%%%n", "毫秒级响应率(<100ms)", mybatisResult.getMillisecondResponseRate(), elasticsearchResult.getMillisecondResponseRate()));

        // 计算性能提升
        if (mybatisResult.getAverageTime() > 0) {
            double timeImprovement = ((mybatisResult.getAverageTime() - elasticsearchResult.getAverageTime()) * 100.0) / mybatisResult.getAverageTime();
            double qpsImprovement = ((elasticsearchResult.getQps() - mybatisResult.getQps()) * 100.0) / mybatisResult.getQps();
            logContent.append("------------------------------------------").append("\n");
            logContent.append(String.format("%-30s | %-25.2f%%%n", "响应时间提升", timeImprovement));
            logContent.append(String.format("%-30s | %-25.2f%%%n", "QPS提升", qpsImprovement));
            
            if (timeImprovement > 0) {
                logContent.append(String.format("%-30s | %-25s%n", "性能结论", "Elasticsearch 性能更优"));
            } else {
                logContent.append(String.format("%-30s | %-25s%n", "性能结论", "MyBatis-Plus 性能更优"));
            }
        }
        logContent.append("==========================================\n");

        // 输出到控制台
        System.out.print(logContent.toString());
    }

    /**
     * 打印单个性能测试结果
     */
    public static void printPerformanceResult(PerformanceResult result) {
        StringBuilder logContent = new StringBuilder();
        
        logContent.append("\n==========================================").append("\n");
        logContent.append("性能测试结果: ").append(result.getTestName()).append("\n");
        logContent.append("==========================================").append("\n");
        logContent.append(String.format("%-30s: %d%n", "总请求数", result.getTotalRequests()));
        logContent.append(String.format("%-30s: %.2f ms%n", "平均响应时间", result.getAverageTime()));
        logContent.append(String.format("%-30s: %.2f ms%n", "最小响应时间", result.getMinTime()));
        logContent.append(String.format("%-30s: %.2f ms%n", "最大响应时间", result.getMaxTime()));
        logContent.append(String.format("%-30s: %.2f%n", "QPS (每秒查询数)", result.getQps()));
        logContent.append(String.format("%-30s: %.2f%%%n", "快速响应率(<10ms)", result.getFastResponseRate()));
        logContent.append(String.format("%-30s: %.2f%%%n", "毫秒级响应率(<100ms)", result.getMillisecondResponseRate()));
        logContent.append("==========================================\n");

        System.out.print(logContent.toString());
    }
}
