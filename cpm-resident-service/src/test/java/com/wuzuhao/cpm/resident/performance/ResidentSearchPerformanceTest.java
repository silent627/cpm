package com.wuzuhao.cpm.resident.performance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.resident.entity.Resident;
import com.wuzuhao.cpm.resident.feign.SearchServiceClient;
import com.wuzuhao.cpm.resident.service.ResidentService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.wuzuhao.cpm.resident.performance.ResidentPerformanceComparator.PerformanceResult;
import static com.wuzuhao.cpm.resident.performance.ResidentPerformanceComparator.executeConcurrentPerformanceTest;
import static com.wuzuhao.cpm.resident.performance.ResidentPerformanceComparator.executePerformanceTest;
import static com.wuzuhao.cpm.resident.performance.ResidentPerformanceComparator.printComparison;
import static com.wuzuhao.cpm.resident.performance.ResidentPerformanceComparator.printPerformanceResult;

/**
 * Elasticsearch vs MyBatis-Plus 居民搜索性能对比测试
 * 
 * 测试内容：
 * 1. 模糊查询速度对比
 * 2. 查询性能对比（平均响应时间、QPS、最小/最大响应时间）
 * 3. 数据量扩展性测试（找出毫秒级检索的最大数据量）
 * 4. 并发性能测试
 * 5. 分页性能测试
 */
@Slf4j
@SpringBootTest
public class ResidentSearchPerformanceTest {

    @Autowired
    private ResidentService residentService;

    @Autowired
    private SearchServiceClient searchServiceClient;

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
     * 场景1：单字段模糊查询速度对比测试
     */
    @Test
    public void testSingleFieldFuzzySearch() {
        log.info("========== 开始单字段模糊查询速度对比测试 ==========");
        
        // 1.1 姓名模糊查询
        testFuzzySearchByField("姓名", "张", "realName");
        
        // 1.2 身份证号模糊查询
        testFuzzySearchByField("身份证号", "123", "idCard");
        
        // 1.3 地址模糊查询
        testFuzzySearchByField("地址", "北京", "currentAddress");
    }

    /**
     * 场景2：多字段组合模糊查询速度对比测试
     */
    @Test
    public void testMultiFieldFuzzySearch() {
        log.info("========== 开始多字段组合模糊查询速度对比测试 ==========");
        
        String keyword = testKeywords.get(random.nextInt(testKeywords.size()));
        int page = 0;
        int size = 10;

        // MyBatis-Plus 查询
        PerformanceResult mybatisResult = executePerformanceTest(
            "MyBatis-Plus-多字段组合查询",
            TEST_ITERATIONS,
            () -> {
                try {
                    @SuppressWarnings("unused")
                    Page<Resident> result = residentService.searchByMyBatisPlus(keyword, page + 1, size);
                    return System.nanoTime();
                } catch (Exception e) {
                    log.error("MyBatis-Plus 查询失败", e);
                    return System.nanoTime();
                }
            }
        );

        // Elasticsearch 查询
        PerformanceResult elasticsearchResult = executePerformanceTest(
            "Elasticsearch-多字段组合查询",
            TEST_ITERATIONS,
            () -> {
                try {
                    @SuppressWarnings("unused")
                    Result<Map<String, Object>> result = searchServiceClient.searchResident(keyword, page, size);
                    return System.nanoTime();
                } catch (Exception e) {
                    log.error("Elasticsearch 查询失败", e);
                    return System.nanoTime();
                }
            }
        );

        printComparison(mybatisResult, elasticsearchResult, "多字段组合模糊查询(" + keyword + ")");
    }

    /**
     * 场景3：查询性能对比测试（平均响应时间、QPS、最小/最大响应时间）
     */
    @Test
    public void testQueryPerformance() {
        log.info("========== 开始查询性能对比测试 ==========");
        
        String keyword = testKeywords.get(random.nextInt(testKeywords.size()));
        int page = 0;
        int size = 10;

        // MyBatis-Plus 性能测试
        PerformanceResult mybatisResult = executePerformanceTest(
            "MyBatis-Plus-查询性能",
            TEST_ITERATIONS,
            () -> {
                try {
                    @SuppressWarnings("unused")
                    Page<Resident> result = residentService.searchByMyBatisPlus(keyword, page + 1, size);
                    return System.nanoTime();
                } catch (Exception e) {
                    log.error("MyBatis-Plus 查询失败", e);
                    return System.nanoTime();
                }
            }
        );

        // Elasticsearch 性能测试
        PerformanceResult elasticsearchResult = executePerformanceTest(
            "Elasticsearch-查询性能",
            TEST_ITERATIONS,
            () -> {
                try {
                    @SuppressWarnings("unused")
                    Result<Map<String, Object>> result = searchServiceClient.searchResident(keyword, page, size);
                    return System.nanoTime();
                } catch (Exception e) {
                    log.error("Elasticsearch 查询失败", e);
                    return System.nanoTime();
                }
            }
        );

        printComparison(mybatisResult, elasticsearchResult, "查询性能对比(" + keyword + ")");
        
        // 输出详细性能指标
        log.info("\n========== MyBatis-Plus 详细性能指标 ==========");
        printPerformanceResult(mybatisResult);
        
        log.info("\n========== Elasticsearch 详细性能指标 ==========");
        printPerformanceResult(elasticsearchResult);
    }

    /**
     * 场景4：数据量扩展性测试（找出毫秒级检索的最大数据量）
     * 注意：此测试需要数据库中有不同数量的数据
     */
    @Test
    public void testDataVolumeScalability() {
        log.info("========== 开始数据量扩展性测试 ==========");
        log.info("注意：此测试需要数据库中有不同数量的数据");
        
        String keyword = "*"; // 查询所有数据
        int[] pageSizes = {10, 20, 50, 100};
        
        for (int size : pageSizes) {
            log.info("\n测试分页大小: {}", size);
            
            // MyBatis-Plus 测试
            PerformanceResult mybatisResult = executePerformanceTest(
                "MyBatis-Plus-数据量扩展性(size=" + size + ")",
                50, // 减少迭代次数以加快测试
                () -> {
                    try {
                        int page = random.nextInt(10);
                        @SuppressWarnings("unused")
                        Page<Resident> result = residentService.searchByMyBatisPlus(keyword, page + 1, size);
                        return System.nanoTime();
                    } catch (Exception e) {
                        log.error("MyBatis-Plus 查询失败", e);
                        return System.nanoTime();
                    }
                }
            );

            // Elasticsearch 测试
            PerformanceResult elasticsearchResult = executePerformanceTest(
                "Elasticsearch-数据量扩展性(size=" + size + ")",
                50,
                () -> {
                    try {
                        int page = random.nextInt(10);
                        @SuppressWarnings("unused")
                        Result<Map<String, Object>> result = searchServiceClient.searchResident(keyword, page, size);
                        return System.nanoTime();
                    } catch (Exception e) {
                        log.error("Elasticsearch 查询失败", e);
                        return System.nanoTime();
                    }
                }
            );

            printComparison(mybatisResult, elasticsearchResult, "数据量扩展性测试(size=" + size + ")");
            
            // 检查毫秒级响应率
            log.info("MyBatis-Plus 毫秒级响应率(<100ms): {:.2f}%", mybatisResult.getMillisecondResponseRate());
            log.info("Elasticsearch 毫秒级响应率(<100ms): {:.2f}%", elasticsearchResult.getMillisecondResponseRate());
        }
    }

    /**
     * 场景5：并发性能测试
     */
    @Test
    public void testConcurrentPerformance() {
        log.info("========== 开始并发性能测试 ==========");
        
        String keyword = testKeywords.get(random.nextInt(testKeywords.size()));
        int page = 0;
        int size = 10;
        int requestsPerThread = 20;

        for (int threadCount : CONCURRENT_THREAD_COUNTS) {
            log.info("\n测试并发级别: {} 线程", threadCount);

            // MyBatis-Plus 并发查询
            PerformanceResult mybatisResult = executeConcurrentPerformanceTest(
                "MyBatis-Plus-并发查询(" + threadCount + "线程)",
                threadCount,
                requestsPerThread,
                () -> {
                    try {
                        @SuppressWarnings("unused")
                        Page<Resident> result = residentService.searchByMyBatisPlus(keyword, page + 1, size);
                        return System.nanoTime();
                    } catch (Exception e) {
                        log.error("MyBatis-Plus 并发查询失败", e);
                        return System.nanoTime();
                    }
                }
            );

            // Elasticsearch 并发查询
            PerformanceResult elasticsearchResult = executeConcurrentPerformanceTest(
                "Elasticsearch-并发查询(" + threadCount + "线程)",
                threadCount,
                requestsPerThread,
                () -> {
                    try {
                        @SuppressWarnings("unused")
                        Result<Map<String, Object>> result = searchServiceClient.searchResident(keyword, page, size);
                        return System.nanoTime();
                    } catch (Exception e) {
                        log.error("Elasticsearch 并发查询失败", e);
                        return System.nanoTime();
                    }
                }
            );

            printComparison(mybatisResult, elasticsearchResult, 
                "并发查询(" + threadCount + "线程, " + requestsPerThread + "请求/线程)");
        }
    }

    /**
     * 场景6：分页性能测试
     */
    @Test
    public void testPaginationPerformance() {
        log.info("========== 开始分页性能测试 ==========");
        
        String keyword = "*"; // 查询所有
        int[] pageSizes = {10, 20, 50, 100};

        for (int size : pageSizes) {
            log.info("\n测试分页大小: {}", size);
            
            // MyBatis-Plus 分页查询
            PerformanceResult mybatisResult = executePerformanceTest(
                "MyBatis-Plus-分页查询(size=" + size + ")",
                TEST_ITERATIONS,
                () -> {
                    try {
                        int page = random.nextInt(10);
                        @SuppressWarnings("unused")
                        Page<Resident> result = residentService.searchByMyBatisPlus(keyword, page + 1, size);
                        return System.nanoTime();
                    } catch (Exception e) {
                        log.error("MyBatis-Plus 分页查询失败", e);
                        return System.nanoTime();
                    }
                }
            );

            // Elasticsearch 分页查询
            PerformanceResult elasticsearchResult = executePerformanceTest(
                "Elasticsearch-分页查询(size=" + size + ")",
                TEST_ITERATIONS,
                () -> {
                    try {
                        int page = random.nextInt(10);
                        @SuppressWarnings("unused")
                        Result<Map<String, Object>> result = searchServiceClient.searchResident(keyword, page, size);
                        return System.nanoTime();
                    } catch (Exception e) {
                        log.error("Elasticsearch 分页查询失败", e);
                        return System.nanoTime();
                    }
                }
            );

            printComparison(mybatisResult, elasticsearchResult, "分页查询(size=" + size + ")");
        }
    }

    /**
     * 场景7：不同关键词长度的查询性能测试
     */
    @Test
    public void testKeywordLengthPerformance() {
        log.info("========== 开始不同关键词长度查询性能测试 ==========");
        
        String[] keywords = {"张", "张三", "张三丰", "张三丰123", "张三丰123456"};
        int page = 0;
        int size = 10;

        for (String keyword : keywords) {
            log.info("\n测试关键词长度: {} (长度: {})", keyword, keyword.length());
            
            // MyBatis-Plus 测试
            PerformanceResult mybatisResult = executePerformanceTest(
                "MyBatis-Plus-关键词长度(" + keyword.length() + "字符)",
                50,
                () -> {
                    try {
                        @SuppressWarnings("unused")
                        Page<Resident> result = residentService.searchByMyBatisPlus(keyword, page + 1, size);
                        return System.nanoTime();
                    } catch (Exception e) {
                        log.error("MyBatis-Plus 查询失败", e);
                        return System.nanoTime();
                    }
                }
            );

            // Elasticsearch 测试
            PerformanceResult elasticsearchResult = executePerformanceTest(
                "Elasticsearch-关键词长度(" + keyword.length() + "字符)",
                50,
                () -> {
                    try {
                        @SuppressWarnings("unused")
                        Result<Map<String, Object>> result = searchServiceClient.searchResident(keyword, page, size);
                        return System.nanoTime();
                    } catch (Exception e) {
                        log.error("Elasticsearch 查询失败", e);
                        return System.nanoTime();
                    }
                }
            );

            printComparison(mybatisResult, elasticsearchResult, "关键词长度测试(" + keyword + ")");
        }
    }

    /**
     * 辅助方法：测试单个字段的模糊查询
     */
    private void testFuzzySearchByField(String fieldName, String keyword, String fieldType) {
        log.info("\n测试字段: {} (关键词: {})", fieldName, keyword);
        int page = 0;
        int size = 10;

        // MyBatis-Plus 查询
        PerformanceResult mybatisResult = executePerformanceTest(
            "MyBatis-Plus-" + fieldName + "模糊查询",
            TEST_ITERATIONS,
            () -> {
                try {
                    @SuppressWarnings("unused")
                    Page<Resident> result = residentService.searchByMyBatisPlus(keyword, page + 1, size);
                    return System.nanoTime();
                } catch (Exception e) {
                    log.error("MyBatis-Plus 查询失败", e);
                    return System.nanoTime();
                }
            }
        );

        // Elasticsearch 查询
        PerformanceResult elasticsearchResult = executePerformanceTest(
            "Elasticsearch-" + fieldName + "模糊查询",
            TEST_ITERATIONS,
            () -> {
                try {
                    @SuppressWarnings("unused")
                    Result<Map<String, Object>> result = searchServiceClient.searchResident(keyword, page, size);
                    return System.nanoTime();
                } catch (Exception e) {
                    log.error("Elasticsearch 查询失败", e);
                    return System.nanoTime();
                }
            }
        );

        printComparison(mybatisResult, elasticsearchResult, fieldName + "模糊查询(" + keyword + ")");
    }
}
