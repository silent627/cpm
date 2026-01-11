package com.wuzuhao.cpm.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch配置类
 * 
 * 使用新的 ElasticsearchClient API (Elasticsearch Java Client)
 */
@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUris;

    @Bean
    public RestClient restClient() {
        // 解析URI，支持多个地址（逗号分隔）
        String[] uris = elasticsearchUris.split(",");
        HttpHost[] hosts = new HttpHost[uris.length];
        for (int i = 0; i < uris.length; i++) {
            String uri = uris[i].trim();
            if (uri.startsWith("http://")) {
                uri = uri.substring(7);
            } else if (uri.startsWith("https://")) {
                uri = uri.substring(8);
            }
            String[] parts = uri.split(":");
            String host = parts[0];
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
            String scheme = elasticsearchUris.contains("https://") ? "https" : "http";
            hosts[i] = new HttpHost(host, port, scheme);
        }
        return RestClient.builder(hosts).build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        // 使用Jackson作为JSON映射器
        return new RestClientTransport(restClient, new JacksonJsonpMapper(new ObjectMapper()));
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}
