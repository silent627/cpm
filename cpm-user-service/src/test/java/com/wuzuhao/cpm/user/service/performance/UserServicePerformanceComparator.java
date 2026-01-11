package com.wuzuhao.cpm.user.service.performance;

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
 * 性能对比工具类
 * 用于对比Redis缓存版本和原先代码的性能差异
 */
public class UserServicePerformanceComparator {

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
        private long cacheHits; // 缓存命中数
        private double cacheHitRate; // 缓存命中率
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
        long cacheHits = 0;

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            long singleStart = System.nanoTime();
            supplier.get(); // 执行测试操作
            long singleEnd = System.nanoTime();
            long duration = singleEnd - singleStart;
            times.add(duration);

            // 根据执行时间判断是否命中缓存（小于5ms认为是缓存命中）
            if (duration < 5_000_000) { // 5毫秒 = 5_000_000 纳秒
                cacheHits++;
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
        double cacheHitRate = iterations > 0 ? (cacheHits * 100.0 / iterations) : 0;

        return new PerformanceResult(testName, totalTime, avgMs, minMs, maxMs, iterations, qps, cacheHits, cacheHitRate);
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
        java.util.concurrent.atomic.AtomicLong cacheHits = new java.util.concurrent.atomic.AtomicLong(0);
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
                            // 根据执行时间判断是否命中缓存（小于5ms认为是缓存命中）
                            if (duration < 5_000_000) { // 5毫秒 = 5_000_000 纳秒
                                cacheHits.incrementAndGet();
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

        // 计算统计数据（使用double类型，保留两位小数）
        double totalMs = times.stream().mapToDouble(time -> time / 1_000_000.0).sum();
        double avgMs = totalMs / totalRequests;
        double minMs = times.stream().mapToDouble(time -> time / 1_000_000.0).min().orElse(0.0);
        double maxMs = times.stream().mapToDouble(time -> time / 1_000_000.0).max().orElse(0.0);
        double qps = (totalRequests * 1_000_000_000.0) / totalTime;
        double cacheHitRate = totalRequests > 0 ? (cacheHits.get() * 100.0 / totalRequests) : 0;

        return new PerformanceResult(testName, totalTime, avgMs, minMs, maxMs, totalRequests, qps, cacheHits.get(), cacheHitRate);
    }

    /**
     * 打印性能对比结果（同时输出到控制台和日志文件）
     */
    public static void printComparison(PerformanceResult original, PerformanceResult cached, String operation) {
        StringBuilder logContent = new StringBuilder();
        
        // 构建输出内容
        logContent.append("\n==========================================").append("\n");
        logContent.append("性能对比测试: ").append(operation).append("\n");
        logContent.append("==========================================").append("\n");
        logContent.append(String.format("%-30s | %-20s | %-20s%n", "指标", "原先代码", "Redis缓存"));
        logContent.append("------------------------------------------").append("\n");
        logContent.append(String.format("%-30s | %-20s | %-20s%n", "测试名称", original.getTestName(), cached.getTestName()));
        logContent.append(String.format("%-30s | %-20d | %-20d%n", "总请求数", original.getTotalRequests(), cached.getTotalRequests()));
        logContent.append(String.format("%-30s | %-20.2f | %-20.2f%n", "平均响应时间(ms)", original.getAverageTime(), cached.getAverageTime()));
        logContent.append(String.format("%-30s | %-20.2f | %-20.2f%n", "最小响应时间(ms)", original.getMinTime(), cached.getMinTime()));
        logContent.append(String.format("%-30s | %-20.2f | %-20.2f%n", "最大响应时间(ms)", original.getMaxTime(), cached.getMaxTime()));
        logContent.append(String.format("%-30s | %-20.2f | %-20.2f%n", "QPS (每秒查询数)", original.getQps(), cached.getQps()));
        logContent.append(String.format("%-30s | %-20.2f%% | %-20.2f%%%n", "缓存命中率", original.getCacheHitRate(), cached.getCacheHitRate()));

        // 计算性能提升
        if (original.getAverageTime() > 0) {
            double improvement = ((original.getAverageTime() - cached.getAverageTime()) * 100.0) / original.getAverageTime();
            double qpsImprovement = ((cached.getQps() - original.getQps()) * 100.0) / original.getQps();
            logContent.append("------------------------------------------").append("\n");
            logContent.append(String.format("%-30s | %-20.2f%%%n", "响应时间提升", improvement));
            logContent.append(String.format("%-30s | %-20.2f%%%n", "QPS提升", qpsImprovement));
        }
        logContent.append("==========================================\n");

        // 输出到控制台
        System.out.print(logContent.toString());

        // 输出到日志文件
        PerformanceTestLogger.writeLog(logContent.toString());
    }
}
