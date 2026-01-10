package com.wuzuhao.cpm.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch配置类
 * 
 * 注意：RestHighLevelClient 已弃用（自Elasticsearch 7.15版本起）
 * 建议迁移到新的 ElasticsearchClient API
 * 当前保留此配置以维持现有功能，后续版本需要升级到新的客户端
 */
@Configuration
@SuppressWarnings("deprecation")
public class ElasticsearchConfig {

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")
                )
        );
    }
}

