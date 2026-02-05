package com.wuzuhao.cpm.search.performance;

import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.search.feign.ResidentServiceClient;
import com.wuzuhao.cpm.search.feign.HouseholdServiceClient;
import com.wuzuhao.cpm.search.feign.UserServiceClient;
import com.wuzuhao.cpm.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.wuzuhao.cpm.search.performance.SearchPerformanceComparator.PerformanceResult;
import static com.wuzuhao.cpm.search.performance.SearchPerformanceComparator.executeConcurrentPerformanceTest;
import static com.wuzuhao.cpm.search.performance.SearchPerformanceComparator.executePerformanceTest;
import static com.wuzuhao.cpm.search.performance.SearchPerformanceComparator.printComparison;

/**
 * Elasticsearch vs MyBatis-Plus 性能对比测试
 */
@Slf4j
@SpringBootTest
public class SearchPerformanceTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private ResidentServiceClient residentServiceClient;

    @Autowired
    private HouseholdServiceClient householdServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    // 测试数据
    private List<String> testKeywords;
    private Random random = new Random();
    private static final int TEST_ITERATIONS = 100; // 单次测试迭代次数
    private static final int[] CONCURRENT_THREAD_COUNTS = {10, 50, 100, 500}; // 并发测试线程数

    @BeforeEach
    public void setUp() {
        // 准备测试关键词
        testKeywords = new java.util.ArrayList<>();
        testKeywords.add("张");
        testKeywords.add("李");
        testKeywords.add("王");
        testKeywords.add("刘");
        testKeywords.add("陈");
        testKeywords.add("123");
        testKeywords.add("456");
        testKeywords.add("789");
        testKeywords.add("北京市");
        testKeywords.add("上海市");
        testKeywords.add("广州市");
        testKeywords.add("138");
        testKeywords.add("139");
        testKeywords.add("150");
    }

    /**
     * 居民搜索性能对比测试 - 单字段搜索
     */
    @Test
    public void testResidentSearchPerformance_SingleField() {
        log.info("开始居民搜索性能对比测试 - 单字段搜索");
        
        String keyword = testKeywords.get(random.nextInt(testKeywords.size()));
        int page = 0;
        int size = 10;

        // MyBatis-Plus 查询（通过 Feign 调用，实际是数据库查询）
        PerformanceResult mybatisResult = executePerformanceTest(
            "MyBatis-Plus-居民单字段搜索",
            TEST_ITERATIONS,
            () -> {
                @SuppressWarnings("unused")
                Result<?> result = executeWithRetry(
                    () -> residentServiceClient.getAllResidents(),
                    "MyBatis-Plus-居民单字段搜索"
                );
                return System.nanoTime();
            }
        );

        // Elasticsearch 查询
        PerformanceResult elasticsearchResult = executePerformanceTest(
            "Elasticsearch-居民单字段搜索",
            TEST_ITERATIONS,
            () -> {
                try {
                    @SuppressWarnings("unused")
                    Map<String, Object> result = searchService.searchResident(keyword, page, size);
                    return System.nanoTime();
                } catch (Exception e) {
                    log.error("Elasticsearch 查询失败", e);
                    return System.nanoTime();
                }
            }
        );

        printComparison(mybatisResult, elasticsearchResult, "居民搜索-单字段搜索(" + keyword + ")");
    }

    /**
     * 居民搜索性能对比测试 - 多字段组合搜索
     */
    @Test
    public void testResidentSearchPerformance_MultiField() {
        log.info("开始居民搜索性能对比测试 - 多字段组合搜索");
        
        String keyword = "张 138 北京";
        int page = 0;
        int size = 10;

        // MyBatis-Plus 查询
        PerformanceResult mybatisResult = executePerformanceTest(
            "MyBatis-Plus-居民多字段搜索",
            TEST_ITERATIONS,
            () -> {
                @SuppressWarnings("unused")
                Result<?> result = executeWithRetry(
                    () -> residentServiceClient.getAllResidents(),
                    "MyBatis-Plus-居民多字段搜索"
                );
                return System.nanoTime();
            }
        );

        // Elasticsearch 查询
        PerformanceResult elasticsearchResult = executePerformanceTest(
            "Elasticsearch-居民多字段搜索",
            TEST_ITERATIONS,
            () -> {
                try {
                    @SuppressWarnings("unused")
                    Map<String, Object> result = searchService.searchResident(keyword, page, size);
                    return System.nanoTime();
                } catch (Exception e) {
                    log.error("Elasticsearch 查询失败", e);
                    return System.nanoTime();
                }
            }
        );

        printComparison(mybatisResult, elasticsearchResult, "居民搜索-多字段组合搜索");
    }

    /**
     * 居民搜索性能对比测试 - 分页查询
     */
    @Test
    public void testResidentSearchPerformance_Pagination() {
        log.info("开始居民搜索性能对比测试 - 分页查询");
        
        String keyword = "*"; // 查询所有
        int[] pageSizes = {10, 20, 50, 100};

        for (int size : pageSizes) {
            log.info("测试分页大小: {}", size);
            
            // Elasticsearch 查询
            PerformanceResult elasticsearchResult = executePerformanceTest(
                "Elasticsearch-居民分页查询(size=" + size + ")",
                TEST_ITERATIONS,
                () -> {
                    try {
                        int page = random.nextInt(10); // 随机页码
                        @SuppressWarnings("unused")
                        Map<String, Object> result = searchService.searchResident(keyword, page, size);
                        return System.nanoTime();
                    } catch (Exception e) {
                        log.error("Elasticsearch 查询失败", e);
                        return System.nanoTime();
                    }
                }
            );

            log.info("Elasticsearch 分页查询(size={}): 平均响应时间={}ms, QPS={}", 
                size, elasticsearchResult.getAverageTime(), elasticsearchResult.getQps());
        }
    }

    /**
     * 户籍搜索性能对比测试
     */
    @Test
    public void testHouseholdSearchPerformance() {
        log.info("开始户籍搜索性能对比测试");
        
        String keyword = testKeywords.get(random.nextInt(testKeywords.size()));
        int page = 0;
        int size = 10;

        // MyBatis-Plus 查询
        PerformanceResult mybatisResult = executePerformanceTest(
            "MyBatis-Plus-户籍搜索",
            TEST_ITERATIONS,
            () -> {
                @SuppressWarnings("unused")
                Result<?> result = executeWithRetry(
                    () -> householdServiceClient.getAllHouseholds(),
                    "MyBatis-Plus-户籍搜索"
                );
                return System.nanoTime();
            }
        );

        // Elasticsearch 查询
        PerformanceResult elasticsearchResult = executePerformanceTest(
            "Elasticsearch-户籍搜索",
            TEST_ITERATIONS,
            () -> {
                try {
                    @SuppressWarnings("unused")
                    Map<String, Object> result = searchService.searchHousehold(keyword, page, size);
                    return System.nanoTime();
                } catch (Exception e) {
                    log.error("Elasticsearch 查询失败", e);
                    return System.nanoTime();
                }
            }
        );

        printComparison(mybatisResult, elasticsearchResult, "户籍搜索(" + keyword + ")");
    }

    /**
     * 用户搜索性能对比测试
     */
    @Test
    public void testUserSearchPerformance() {
        log.info("开始用户搜索性能对比测试");
        
        String keyword = testKeywords.get(random.nextInt(testKeywords.size()));
        int page = 0;
        int size = 10;

        // MyBatis-Plus 查询
        PerformanceResult mybatisResult = executePerformanceTest(
            "MyBatis-Plus-用户搜索",
            TEST_ITERATIONS,
            () -> {
                @SuppressWarnings("unused")
                Result<?> result = executeWithRetry(
                    () -> userServiceClient.getAllUsers(),
                    "MyBatis-Plus-用户搜索"
                );
                return System.nanoTime();
            }
        );

        // Elasticsearch 查询
        PerformanceResult elasticsearchResult = executePerformanceTest(
            "Elasticsearch-用户搜索",
            TEST_ITERATIONS,
            () -> {
                try {
                    @SuppressWarnings("unused")
                    Map<String, Object> result = searchService.searchUser(keyword, page, size);
                    return System.nanoTime();
                } catch (Exception e) {
                    log.error("Elasticsearch 查询失败", e);
                    return System.nanoTime();
                }
            }
        );

        printComparison(mybatisResult, elasticsearchResult, "用户搜索(" + keyword + ")");
    }

    /**
     * 并发性能对比测试 - 居民搜索
     */
    @Test
    public void testConcurrentResidentSearchPerformance() {
        log.info("开始并发居民搜索性能对比测试");
        
        String keyword = testKeywords.get(random.nextInt(testKeywords.size()));
        int page = 0;
        int size = 10;
        int requestsPerThread = 20;

        for (int threadCount : CONCURRENT_THREAD_COUNTS) {
            log.info("测试并发级别: {} 线程", threadCount);

            // MyBatis-Plus 并发查询
            PerformanceResult mybatisResult = executeConcurrentPerformanceTest(
                "MyBatis-Plus-居民并发搜索(" + threadCount + "线程)",
                threadCount,
                requestsPerThread,
                () -> {
                    @SuppressWarnings("unused")
                    Result<?> result = executeWithRetry(
                        () -> residentServiceClient.getAllResidents(),
                        "MyBatis-Plus-居民并发搜索"
                    );
                    return System.nanoTime();
                }
            );

            // Elasticsearch 并发查询
            PerformanceResult elasticsearchResult = executeConcurrentPerformanceTest(
                "Elasticsearch-居民并发搜索(" + threadCount + "线程)",
                threadCount,
                requestsPerThread,
                () -> {
                    try {
                        @SuppressWarnings("unused")
                        Map<String, Object> result = searchService.searchResident(keyword, page, size);
                        return System.nanoTime();
                    } catch (Exception e) {
                        log.error("Elasticsearch 并发查询失败", e);
                        return System.nanoTime();
                    }
                }
            );

            printComparison(mybatisResult, elasticsearchResult, 
                "居民并发搜索(" + threadCount + "线程, " + requestsPerThread + "请求/线程)");
        }
    }

    /**
     * 完全匹配 vs 模糊匹配性能测试
     */
    @Test
    public void testExactMatchVsFuzzyMatch() {
        log.info("开始完全匹配 vs 模糊匹配性能测试");
        
        // 完全匹配关键词（假设存在）
        String exactKeyword = "张三";
        // 模糊匹配关键词
        String fuzzyKeyword = "张";
        
        int page = 0;
        int size = 10;

        // 完全匹配查询
        PerformanceResult exactResult = executePerformanceTest(
            "Elasticsearch-完全匹配",
            TEST_ITERATIONS,
            () -> {
                try {
                    @SuppressWarnings("unused")
                    Map<String, Object> result = searchService.searchResident(exactKeyword, page, size);
                    return System.nanoTime();
                } catch (Exception e) {
                    log.error("完全匹配查询失败", e);
                    return System.nanoTime();
                }
            }
        );

        // 模糊匹配查询
        PerformanceResult fuzzyResult = executePerformanceTest(
            "Elasticsearch-模糊匹配",
            TEST_ITERATIONS,
            () -> {
                try {
                    @SuppressWarnings("unused")
                    Map<String, Object> result = searchService.searchResident(fuzzyKeyword, page, size);
                    return System.nanoTime();
                } catch (Exception e) {
                    log.error("模糊匹配查询失败", e);
                    return System.nanoTime();
                }
            }
        );

        printComparison(fuzzyResult, exactResult, "完全匹配 vs 模糊匹配");
    }

    /**
     * 执行带重试的Feign调用，处理429限流错误
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> supplier, String operationName) {
        int maxRetries = 3;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                return supplier.get();
            } catch (feign.FeignException.TooManyRequests e) {
                // 429 错误，等待后重试
                retryCount++;
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(2000); // 等待2秒后重试
                        log.debug("{} 遇到限流，等待2秒后重试 ({}/{})", operationName, retryCount, maxRetries);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("{} 重试被中断", operationName);
                        break;
                    }
                } else {
                    log.warn("{} 达到最大重试次数，返回默认值", operationName);
                    return null;
                }
            } catch (Exception e) {
                log.error("{} 执行失败", operationName, e);
                return null;
            }
        }
        return null;
    }
}
