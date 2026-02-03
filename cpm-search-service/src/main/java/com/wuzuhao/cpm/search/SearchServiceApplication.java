package com.wuzuhao.cpm.search;

import com.wuzuhao.cpm.search.service.SearchService;
import com.wuzuhao.cpm.search.util.ElasticsearchIndexUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 搜索服务启动类
 */
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = "com.wuzuhao.cpm")
public class SearchServiceApplication implements CommandLineRunner {

    @Autowired
    private ElasticsearchIndexUtil indexUtil;

    @Autowired(required = false)
    private SearchService searchService;

    @Value("${elasticsearch.index.auto-sync-on-startup:false}")
    private boolean autoSyncOnStartup;

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("开始初始化 Elasticsearch 索引结构...");
            indexUtil.initIndices();
            log.info("Elasticsearch 索引结构初始化完成");

            // 如果配置了自动同步，则在启动时同步数据
            if (autoSyncOnStartup && searchService != null) {
                log.info("检测到配置 elasticsearch.index.auto-sync-on-startup=true，开始自动同步数据...");
                try {
                    // 延迟几秒，确保其他服务已经启动完成
                    Thread.sleep(5000);
                    searchService.rebuildIndex();
                    log.info("启动时数据同步完成");
                } catch (Exception e) {
                    log.error("启动时数据同步失败，您可以稍后通过 /search/index/rebuild 接口手动重建索引。错误信息: {}", e.getMessage(), e);
                    // 不抛出异常，允许应用继续启动
                }
            } else {
                log.info("未启用启动时自动同步数据（elasticsearch.index.auto-sync-on-startup=false）");
                log.info("如需同步数据，请调用 /search/index/rebuild 接口");
            }
        } catch (Exception e) {
            log.error("Elasticsearch 索引初始化失败，应用将继续启动。错误信息: {}", e.getMessage(), e);
            // 不抛出异常，允许应用继续启动
            // 用户可以通过 /search/index/rebuild 接口手动重建索引
        }
    }
}

