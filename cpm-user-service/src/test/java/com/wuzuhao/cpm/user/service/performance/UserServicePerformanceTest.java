package com.wuzuhao.cpm.user.service.performance;

import com.wuzuhao.cpm.user.entity.User;
import com.wuzuhao.cpm.user.service.UserService;
import com.wuzuhao.cpm.util.RedisUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.wuzuhao.cpm.user.service.performance.UserServicePerformanceComparator.PerformanceResult;
import static com.wuzuhao.cpm.user.service.performance.UserServicePerformanceComparator.executeConcurrentPerformanceTest;
import static com.wuzuhao.cpm.user.service.performance.UserServicePerformanceComparator.executePerformanceTest;
import static com.wuzuhao.cpm.user.service.performance.UserServicePerformanceComparator.printComparison;

/**
 * UserService性能对比测试
 * 测试Redis缓存优化前后的性能差异
 */
@SpringBootTest
public class UserServicePerformanceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private OriginalUserService originalUserService;

    @Autowired
    private RedisUtil redisUtil;

    // 测试数据
    private List<Long> testUserIds = new ArrayList<>();
    private List<String> testUsernames = new ArrayList<>();
    private List<String> testEmails = new ArrayList<>();
    private static final int TEST_DATA_SIZE = 1000; // 测试数据量

    @BeforeEach
    public void setUp() {
        // 清空Redis缓存
        clearAllCache();

        // 准备测试数据
        prepareTestData();
    }

    /**
     * 准备测试数据
     */
    private void prepareTestData() {
        testUserIds.clear();
        testUsernames.clear();
        testEmails.clear();

        // 检查数据库中是否有足够的测试数据
        List<User> existingUsers = userService.list();
        if (existingUsers.size() < TEST_DATA_SIZE) {
            // 创建测试数据
            for (int i = 0; i < TEST_DATA_SIZE; i++) {
                User user = new User();
                user.setUsername("testuser" + System.currentTimeMillis() + "_" + i);
                user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
                user.setRealName("测试用户" + i);
                user.setPhone("138" + String.format("%08d", i));
                user.setEmail("test" + i + "@example.com");
                user.setRole("USER");
                user.setStatus(1);
                userService.save(user);
                existingUsers.add(user);
            }
        }

        // 获取测试用户ID列表
        for (User user : existingUsers) {
            if (user.getId() != null) {
                testUserIds.add(user.getId());
                if (user.getUsername() != null) {
                    testUsernames.add(user.getUsername());
                }
                if (user.getEmail() != null) {
                    testEmails.add(user.getEmail());
                }
                if (testUserIds.size() >= TEST_DATA_SIZE) {
                    break;
                }
            }
        }
    }

    /**
     * 清空所有缓存
     */
    private void clearAllCache() {
        redisUtil.deleteByPattern("user:*");
    }

    // ==================== Redis基础操作测试 ====================

    @Test
    public void testRedisBasicOperations() {
        System.out.println("\n==========================================");
        System.out.println("Redis基础操作测试");
        System.out.println("==========================================");

        String testKey = "test:key:1";
        String testValue = "test_value";

        // 测试set操作
        long start = System.nanoTime();
        redisUtil.set(testKey, testValue, 60);
        long setTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println("SET操作耗时: " + setTime + " ms");

        // 测试get操作
        start = System.nanoTime();
        Object value = redisUtil.get(testKey);
        long getTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println("GET操作耗时: " + getTime + " ms");
        System.out.println("GET操作返回值: " + value);

        // 测试hasKey操作
        start = System.nanoTime();
        boolean exists = redisUtil.hasKey(testKey);
        long hasKeyTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println("HASKEY操作耗时: " + hasKeyTime + " ms");
        System.out.println("HASKEY操作返回值: " + exists);

        // 测试expire操作
        start = System.nanoTime();
        redisUtil.expire(testKey, 120);
        long expireTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println("EXPIRE操作耗时: " + expireTime + " ms");

        // 测试getExpire操作
        start = System.nanoTime();
        long expire = redisUtil.getExpire(testKey);
        long getExpireTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println("GETEXPIRE操作耗时: " + getExpireTime + " ms");
        System.out.println("GETEXPIRE操作返回值: " + expire + " 秒");

        // 测试del操作
        start = System.nanoTime();
        redisUtil.del(testKey);
        long delTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println("DEL操作耗时: " + delTime + " ms");

        System.out.println("==========================================\n");

        // 记录到日志文件
        PerformanceTestLogger.writeLog("\n==========================================");
        PerformanceTestLogger.writeLog("Redis基础操作测试");
        PerformanceTestLogger.writeLog("==========================================");
        PerformanceTestLogger.writeLog("SET操作耗时: %d ms", setTime);
        PerformanceTestLogger.writeLog("GET操作耗时: %d ms", getTime);
        PerformanceTestLogger.writeLog("GET操作返回值: %s", value);
        PerformanceTestLogger.writeLog("HASKEY操作耗时: %d ms", hasKeyTime);
        PerformanceTestLogger.writeLog("HASKEY操作返回值: %s", exists);
        PerformanceTestLogger.writeLog("EXPIRE操作耗时: %d ms", expireTime);
        PerformanceTestLogger.writeLog("GETEXPIRE操作耗时: %d ms", getExpireTime);
        PerformanceTestLogger.writeLog("GETEXPIRE操作返回值: %d 秒", expire);
        PerformanceTestLogger.writeLog("DEL操作耗时: %d ms", delTime);
        PerformanceTestLogger.writeLog("==========================================\n");
    }

    // ==================== 单次查询性能对比 ====================

    @Test
    public void testSingleQueryPerformance() {
        if (testUserIds.isEmpty()) {
            System.out.println("测试数据不足，跳过测试");
            return;
        }

        Long testId = testUserIds.get(0);
        int iterations = 100;

        // 清空缓存，测试首次查询
        clearAllCache();

        // 原先代码性能测试（首次查询，缓存未命中）
        PerformanceResult originalResult = executePerformanceTest(
                "原先代码-根据ID查询",
                iterations,
                () -> {
                    User user = originalUserService.getById(testId);
                    return user != null ? System.nanoTime() : null;
                }
        );

        // Redis缓存版本性能测试（首次查询，缓存未命中）
        clearAllCache();
        PerformanceResult cachedFirstResult = executePerformanceTest(
                "Redis缓存-根据ID查询(首次)",
                iterations,
                () -> {
                    User user = userService.getById(testId);
                    return user != null ? System.nanoTime() : null;
                }
        );

        // Redis缓存版本性能测试（二次查询，缓存命中）
        PerformanceResult cachedSecondResult = executePerformanceTest(
                "Redis缓存-根据ID查询(缓存命中)",
                iterations,
                () -> {
                    User user = userService.getById(testId);
                    return user != null ? System.nanoTime() : null;
                }
        );

        printComparison(originalResult, cachedFirstResult, "根据ID查询-首次查询对比");
        printComparison(originalResult, cachedSecondResult, "根据ID查询-缓存命中对比");
    }

    @Test
    public void testSingleQueryByUsernamePerformance() {
        if (testUsernames.isEmpty()) {
            System.out.println("测试数据不足，跳过测试");
            return;
        }

        String testUsername = testUsernames.get(0);
        int iterations = 100;

        // 清空缓存
        clearAllCache();

        // 原先代码性能测试
        PerformanceResult originalResult = executePerformanceTest(
                "原先代码-根据用户名查询",
                iterations,
                () -> {
                    User user = originalUserService.getByUsername(testUsername);
                    return user != null ? System.nanoTime() : null;
                }
        );

        // Redis缓存版本性能测试（首次查询）
        clearAllCache();
        PerformanceResult cachedFirstResult = executePerformanceTest(
                "Redis缓存-根据用户名查询(首次)",
                iterations,
                () -> {
                    User user = userService.getByUsername(testUsername);
                    return user != null ? System.nanoTime() : null;
                }
        );

        // Redis缓存版本性能测试（二次查询）
        PerformanceResult cachedSecondResult = executePerformanceTest(
                "Redis缓存-根据用户名查询(缓存命中)",
                iterations,
                () -> {
                    User user = userService.getByUsername(testUsername);
                    return user != null ? System.nanoTime() : null;
                }
        );

        printComparison(originalResult, cachedFirstResult, "根据用户名查询-首次查询对比");
        printComparison(originalResult, cachedSecondResult, "根据用户名查询-缓存命中对比");
    }

    @Test
    public void testSingleQueryByEmailPerformance() {
        if (testEmails.isEmpty()) {
            System.out.println("测试数据不足，跳过测试");
            return;
        }

        String testEmail = testEmails.get(0);
        int iterations = 100;

        // 清空缓存
        clearAllCache();

        // 原先代码性能测试
        PerformanceResult originalResult = executePerformanceTest(
                "原先代码-根据邮箱查询",
                iterations,
                () -> {
                    User user = originalUserService.getByEmail(testEmail);
                    return user != null ? System.nanoTime() : null;
                }
        );

        // Redis缓存版本性能测试（首次查询）
        clearAllCache();
        PerformanceResult cachedFirstResult = executePerformanceTest(
                "Redis缓存-根据邮箱查询(首次)",
                iterations,
                () -> {
                    User user = userService.getByEmail(testEmail);
                    return user != null ? System.nanoTime() : null;
                }
        );

        // Redis缓存版本性能测试（二次查询）
        PerformanceResult cachedSecondResult = executePerformanceTest(
                "Redis缓存-根据邮箱查询(缓存命中)",
                iterations,
                () -> {
                    User user = userService.getByEmail(testEmail);
                    return user != null ? System.nanoTime() : null;
                }
        );

        printComparison(originalResult, cachedFirstResult, "根据邮箱查询-首次查询对比");
        printComparison(originalResult, cachedSecondResult, "根据邮箱查询-缓存命中对比");
    }

    // ==================== 高并发性能测试 ====================

    /**
     * 高并发根据ID查询测试 - 多级别并发
     */
    @Test
    public void testHighConcurrencyGetById() {
        if (testUserIds.isEmpty()) {
            System.out.println("测试数据不足，跳过高并发ID查询测试");
            return;
        }

        int[] threadCounts = {100, 500, 1000, 2000};
        int requestsPerThread = 20;
        // 用于缓存命中测试的固定ID集合（使用前20个ID，确保在缓存中）
        int cachedIdCount = Math.min(20, testUserIds.size());
        List<Long> cachedIds = new ArrayList<>(testUserIds.subList(0, cachedIdCount));

        for (int threadCount : threadCounts) {
            System.out.println("\n高并发根据ID查询测试 - 线程数: " + threadCount + ", 每线程请求数: " + requestsPerThread);

            // 清空缓存
            clearAllCache();

            // 原先代码高并发测试
            PerformanceResult originalResult = executeConcurrentPerformanceTest(
                    "原先代码-高并发ID查询(" + threadCount + "线程)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        Long testId = testUserIds.get(ThreadLocalRandom.current().nextInt(testUserIds.size()));
                        User user = originalUserService.getById(testId);
                        return user != null ? System.nanoTime() : null;
                    }
            );

            // Redis缓存版本高并发测试（首次查询）
            clearAllCache();
            // 先预热缓存：将固定ID集合的数据加载到缓存
            for (Long id : cachedIds) {
                userService.getById(id);
            }
            PerformanceResult cachedFirstResult = executeConcurrentPerformanceTest(
                    "Redis缓存-高并发ID查询(" + threadCount + "线程,首次)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        Long testId = testUserIds.get(ThreadLocalRandom.current().nextInt(testUserIds.size()));
                        User user = userService.getById(testId);
                        return user != null ? System.nanoTime() : null;
                    }
            );

            // Redis缓存版本高并发测试（二次查询，缓存命中 - 只查询缓存中的ID）
            PerformanceResult cachedSecondResult = executeConcurrentPerformanceTest(
                    "Redis缓存-高并发ID查询(" + threadCount + "线程,缓存命中)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        Long testId = cachedIds.get(ThreadLocalRandom.current().nextInt(cachedIds.size()));
                        User user = userService.getById(testId);
                        return user != null ? System.nanoTime() : null;
                    }
            );

            printComparison(originalResult, cachedFirstResult, "高并发ID查询(" + threadCount + "线程)-首次查询对比");
            printComparison(originalResult, cachedSecondResult, "高并发ID查询(" + threadCount + "线程)-缓存命中对比");
        }
    }

    /**
     * 高并发根据用户名查询测试 - 多级别并发
     */
    @Test
    public void testHighConcurrencyGetByUsername() {
        if (testUsernames.isEmpty()) {
            System.out.println("测试数据不足，跳过高并发用户名查询测试");
            return;
        }

        int[] threadCounts = {100, 500, 1000, 2000};
        int requestsPerThread = 20;
        String testUsername = testUsernames.get(0);

        for (int threadCount : threadCounts) {
            System.out.println("\n高并发根据用户名查询测试 - 线程数: " + threadCount + ", 每线程请求数: " + requestsPerThread);

            // 清空缓存
            clearAllCache();

            // 原先代码高并发测试
            PerformanceResult originalResult = executeConcurrentPerformanceTest(
                    "原先代码-高并发用户名查询(" + threadCount + "线程)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        User user = originalUserService.getByUsername(testUsername);
                        return user != null ? System.nanoTime() : null;
                    }
            );

            // Redis缓存版本高并发测试（首次查询）
            clearAllCache();
            PerformanceResult cachedFirstResult = executeConcurrentPerformanceTest(
                    "Redis缓存-高并发用户名查询(" + threadCount + "线程,首次)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        User user = userService.getByUsername(testUsername);
                        return user != null ? System.nanoTime() : null;
                    }
            );

            // Redis缓存版本高并发测试（二次查询，缓存命中）
            PerformanceResult cachedSecondResult = executeConcurrentPerformanceTest(
                    "Redis缓存-高并发用户名查询(" + threadCount + "线程,缓存命中)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        User user = userService.getByUsername(testUsername);
                        return user != null ? System.nanoTime() : null;
                    }
            );

            printComparison(originalResult, cachedFirstResult, "高并发用户名查询(" + threadCount + "线程)-首次查询对比");
            printComparison(originalResult, cachedSecondResult, "高并发用户名查询(" + threadCount + "线程)-缓存命中对比");
        }
    }

    /**
     * 高并发根据邮箱查询测试 - 多级别并发
     */
    @Test
    public void testHighConcurrencyGetByEmail() {
        if (testEmails.isEmpty()) {
            System.out.println("测试数据不足，跳过高并发邮箱查询测试");
            return;
        }

        int[] threadCounts = {100, 500, 1000, 2000};
        int requestsPerThread = 20;
        String testEmail = testEmails.get(0);

        for (int threadCount : threadCounts) {
            System.out.println("\n高并发根据邮箱查询测试 - 线程数: " + threadCount + ", 每线程请求数: " + requestsPerThread);

            // 清空缓存
            clearAllCache();

            // 原先代码高并发测试
            PerformanceResult originalResult = executeConcurrentPerformanceTest(
                    "原先代码-高并发邮箱查询(" + threadCount + "线程)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        User user = originalUserService.getByEmail(testEmail);
                        return user != null ? System.nanoTime() : null;
                    }
            );

            // Redis缓存版本高并发测试（首次查询）
            clearAllCache();
            PerformanceResult cachedFirstResult = executeConcurrentPerformanceTest(
                    "Redis缓存-高并发邮箱查询(" + threadCount + "线程,首次)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        User user = userService.getByEmail(testEmail);
                        return user != null ? System.nanoTime() : null;
                    }
            );

            // Redis缓存版本高并发测试（二次查询，缓存命中）
            PerformanceResult cachedSecondResult = executeConcurrentPerformanceTest(
                    "Redis缓存-高并发邮箱查询(" + threadCount + "线程,缓存命中)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        User user = userService.getByEmail(testEmail);
                        return user != null ? System.nanoTime() : null;
                    }
            );

            printComparison(originalResult, cachedFirstResult, "高并发邮箱查询(" + threadCount + "线程)-首次查询对比");
            printComparison(originalResult, cachedSecondResult, "高并发邮箱查询(" + threadCount + "线程)-缓存命中对比");
        }
    }

    /**
     * 高并发混合查询测试 - 模拟真实业务场景
     */
    @Test
    public void testHighConcurrencyMixedQueries() {
        if (testUserIds.isEmpty() || testUsernames.isEmpty() || testEmails.isEmpty()) {
            System.out.println("测试数据不足，跳过高并发混合查询测试");
            return;
        }

        int[] threadCounts = {500, 1000, 2000};
        int requestsPerThread = 30;

        for (int threadCount : threadCounts) {
            System.out.println("\n高并发混合查询测试 - 线程数: " + threadCount + ", 每线程请求数: " + requestsPerThread);

            // 清空缓存
            clearAllCache();

            // 原先代码高并发混合查询测试
            PerformanceResult originalResult = executeConcurrentPerformanceTest(
                    "原先代码-高并发混合查询(" + threadCount + "线程)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        Random random = ThreadLocalRandom.current();
                        int queryType = random.nextInt(3);
                        User user = null;
                        if (queryType == 0) {
                            Long testId = testUserIds.get(random.nextInt(testUserIds.size()));
                            user = originalUserService.getById(testId);
                        } else if (queryType == 1) {
                            String testUsername = testUsernames.get(random.nextInt(testUsernames.size()));
                            user = originalUserService.getByUsername(testUsername);
                        } else {
                            String testEmail = testEmails.get(random.nextInt(testEmails.size()));
                            user = originalUserService.getByEmail(testEmail);
                        }
                        return user != null ? System.nanoTime() : null;
                    }
            );

            // Redis缓存版本高并发混合查询测试（首次查询）
            clearAllCache();
            PerformanceResult cachedFirstResult = executeConcurrentPerformanceTest(
                    "Redis缓存-高并发混合查询(" + threadCount + "线程,首次)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        Random random = ThreadLocalRandom.current();
                        int queryType = random.nextInt(3);
                        User user = null;
                        if (queryType == 0) {
                            Long testId = testUserIds.get(random.nextInt(testUserIds.size()));
                            user = userService.getById(testId);
                        } else if (queryType == 1) {
                            String testUsername = testUsernames.get(random.nextInt(testUsernames.size()));
                            user = userService.getByUsername(testUsername);
                        } else {
                            String testEmail = testEmails.get(random.nextInt(testEmails.size()));
                            user = userService.getByEmail(testEmail);
                        }
                        return user != null ? System.nanoTime() : null;
                    }
            );

            // Redis缓存版本高并发混合查询测试（二次查询，缓存命中）
            PerformanceResult cachedSecondResult = executeConcurrentPerformanceTest(
                    "Redis缓存-高并发混合查询(" + threadCount + "线程,缓存命中)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        Random random = ThreadLocalRandom.current();
                        int queryType = random.nextInt(3);
                        User user = null;
                        if (queryType == 0) {
                            Long testId = testUserIds.get(random.nextInt(testUserIds.size()));
                            user = userService.getById(testId);
                        } else if (queryType == 1) {
                            String testUsername = testUsernames.get(random.nextInt(testUsernames.size()));
                            user = userService.getByUsername(testUsername);
                        } else {
                            String testEmail = testEmails.get(random.nextInt(testEmails.size()));
                            user = userService.getByEmail(testEmail);
                        }
                        return user != null ? System.nanoTime() : null;
                    }
            );

            printComparison(originalResult, cachedFirstResult, "高并发混合查询(" + threadCount + "线程)-首次查询对比");
            printComparison(originalResult, cachedSecondResult, "高并发混合查询(" + threadCount + "线程)-缓存命中对比");
        }
    }

    /**
     * 高并发性能测试 - 根据ID查询（原有方法，优化为多级别并发测试）
     */
    @Test
    public void testHighConcurrencyPerformance() {
        if (testUserIds.isEmpty()) {
            System.out.println("测试数据不足，跳过高并发测试");
            return;
        }

        int[] threadCounts = {500, 1000, 2000, 5000};
        int requestsPerThread = 20;
        // 用于缓存命中测试的固定ID集合（使用前20个ID，确保在缓存中）
        int cachedIdCount = Math.min(20, testUserIds.size());
        List<Long> cachedIds = new ArrayList<>(testUserIds.subList(0, cachedIdCount));

        for (int threadCount : threadCounts) {
            System.out.println("\n高并发性能测试 - 线程数: " + threadCount + ", 每线程请求数: " + requestsPerThread);

            // 清空缓存
            clearAllCache();

            // 原先代码高并发测试
            PerformanceResult originalResult = executeConcurrentPerformanceTest(
                    "原先代码-高并发查询(" + threadCount + "线程)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        Long testId = testUserIds.get(ThreadLocalRandom.current().nextInt(testUserIds.size()));
                        User user = originalUserService.getById(testId);
                        return user != null ? System.nanoTime() : null;
                    }
            );

            // Redis缓存版本高并发测试（首次查询）
            clearAllCache();
            // 先预热缓存：将固定ID集合的数据加载到缓存
            for (Long id : cachedIds) {
                userService.getById(id);
            }
            PerformanceResult cachedFirstResult = executeConcurrentPerformanceTest(
                    "Redis缓存-高并发查询(" + threadCount + "线程,首次)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        Long testId = testUserIds.get(ThreadLocalRandom.current().nextInt(testUserIds.size()));
                        User user = userService.getById(testId);
                        return user != null ? System.nanoTime() : null;
                    }
            );

            // Redis缓存版本高并发测试（二次查询，缓存命中 - 只查询缓存中的ID）
            PerformanceResult cachedSecondResult = executeConcurrentPerformanceTest(
                    "Redis缓存-高并发查询(" + threadCount + "线程,缓存命中)",
                    threadCount,
                    requestsPerThread,
                    () -> {
                        Long testId = cachedIds.get(ThreadLocalRandom.current().nextInt(cachedIds.size()));
                        User user = userService.getById(testId);
                        return user != null ? System.nanoTime() : null;
                    }
            );

            printComparison(originalResult, cachedFirstResult, "高并发查询(" + threadCount + "线程)-首次查询对比");
            printComparison(originalResult, cachedSecondResult, "高并发查询(" + threadCount + "线程)-缓存命中对比");
        }
    }

    // ==================== 缓存命中率测试 ====================

    @Test
    public void testCacheHitRate() {
        if (testUserIds.isEmpty()) {
            System.out.println("测试数据不足，跳过缓存命中率测试");
            return;
        }

        System.out.println("\n==========================================");
        System.out.println("缓存命中率测试");
        System.out.println("==========================================");

        int totalRequests = 1000;
        Long testId = testUserIds.get(0);

        // 清空缓存
        clearAllCache();

        // 首次查询（缓存未命中）
        int firstQueryCount = 100;
        for (int i = 0; i < firstQueryCount; i++) {
            userService.getById(testId);
        }

        // 后续查询（缓存命中）
        int secondQueryCount = totalRequests - firstQueryCount;
        long cacheHitStart = System.nanoTime();
        for (int i = 0; i < secondQueryCount; i++) {
            userService.getById(testId);
        }
        long cacheHitTime = (System.nanoTime() - cacheHitStart) / 1_000_000;

        System.out.println("总请求数: " + totalRequests);
        System.out.println("首次查询数（缓存未命中）: " + firstQueryCount);
        System.out.println("后续查询数（缓存命中）: " + secondQueryCount);
        System.out.println("缓存命中率: " + (secondQueryCount * 100.0 / totalRequests) + "%");
        System.out.println("缓存命中查询总耗时: " + cacheHitTime + " ms");
        System.out.println("缓存命中平均耗时: " + (cacheHitTime / (double) secondQueryCount) + " ms");
        System.out.println("==========================================\n");

        // 记录到日志文件
        PerformanceTestLogger.writeLog("\n==========================================");
        PerformanceTestLogger.writeLog("缓存命中率测试");
        PerformanceTestLogger.writeLog("==========================================");
        PerformanceTestLogger.writeLog("总请求数: %d", totalRequests);
        PerformanceTestLogger.writeLog("首次查询数（缓存未命中）: %d", firstQueryCount);
        PerformanceTestLogger.writeLog("后续查询数（缓存命中）: %d", secondQueryCount);
        PerformanceTestLogger.writeLog("缓存命中率: %.2f%%", (secondQueryCount * 100.0 / totalRequests));
        PerformanceTestLogger.writeLog("缓存命中查询总耗时: %d ms", cacheHitTime);
        PerformanceTestLogger.writeLog("缓存命中平均耗时: %.2f ms", (cacheHitTime / (double) secondQueryCount));
        PerformanceTestLogger.writeLog("==========================================\n");
    }

    /**
     * 测试结束后关闭日志文件
     */
    @AfterAll
    public static void tearDown() {
        PerformanceTestLogger.close();
    }
}
