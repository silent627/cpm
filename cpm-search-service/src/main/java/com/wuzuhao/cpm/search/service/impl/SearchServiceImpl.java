package com.wuzuhao.cpm.search.service.impl;

import com.wuzuhao.cpm.search.service.SearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索服务实现
 * 
 * 注意：RestHighLevelClient 已弃用，后续版本需要迁移到新的 ElasticsearchClient API
 */
@Service
@SuppressWarnings("deprecation")
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    private static final String RESIDENT_INDEX = "resident_index";
    private static final String HOUSEHOLD_INDEX = "household_index";

    @Override
    public Map<String, Object> searchResident(String keyword, Integer page, Integer size) {
        try {
            SearchRequest searchRequest = new SearchRequest(RESIDENT_INDEX);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            // 多字段搜索
            sourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "realName", "idCard", "currentAddress"));
            sourceBuilder.from(page * size);
            sourceBuilder.size(size);
            
            searchRequest.source(sourceBuilder);
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
            return buildSearchResult(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> searchHousehold(String keyword, Integer page, Integer size) {
        try {
            SearchRequest searchRequest = new SearchRequest(HOUSEHOLD_INDEX);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            sourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "householdNumber", "address"));
            sourceBuilder.from(page * size);
            sourceBuilder.size(size);
            
            searchRequest.source(sourceBuilder);
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
            return buildSearchResult(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @Override
    public void rebuildIndex() {
        // TODO: 实现索引重建逻辑
        // 从数据库读取数据，同步到Elasticsearch
    }

    private Map<String, Object> buildSearchResult(SearchResponse response) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> hits = new ArrayList<>();
        
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> source = hit.getSourceAsMap();
            source.put("_id", hit.getId());
            hits.add(source);
        }
        
        result.put("total", response.getHits().getTotalHits().value);
        result.put("hits", hits);
        return result;
    }
}

