package com.wuzuhao.cpm.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.search.feign.HouseholdServiceClient;
import com.wuzuhao.cpm.search.feign.ResidentServiceClient;
import com.wuzuhao.cpm.search.service.SearchService;
import com.wuzuhao.cpm.search.util.ElasticsearchIndexUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索服务实现
 * 
 * 使用新的 ElasticsearchClient API 实现多字段全文检索
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ElasticsearchIndexUtil indexUtil;

    @Autowired
    private ResidentServiceClient residentServiceClient;

    @Autowired
    private HouseholdServiceClient householdServiceClient;

    @Override
    public Map<String, Object> searchResident(String keyword, Integer page, Integer size) {
        try {
            // 构建多字段查询，配置字段权重
            Query multiMatchQuery = Query.of(q -> q
                .multiMatch(MultiMatchQuery.of(m -> m
                    .query(keyword)
                    .fields("realName^3.0", "idCard^2.5", "registeredAddress^1.5", 
                           "currentAddress^1.5", "contactPhone^1.0")
                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                    .fuzziness("AUTO")
                ))
            );

            // 构建搜索请求
            @SuppressWarnings("rawtypes")
            SearchResponse<Map> response = elasticsearchClient.search(
                SearchRequest.of(s -> s
                    .index(ElasticsearchIndexUtil.RESIDENT_INDEX)
                    .query(multiMatchQuery)
                    .from(page * size)
                    .size(size)
                ),
                Map.class
            );

            return buildSearchResult(response);
        } catch (Exception e) {
            log.error("搜索居民信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0L);
            result.put("hits", new ArrayList<>());
            return result;
        }
    }

    @Override
    public Map<String, Object> searchHousehold(String keyword, Integer page, Integer size) {
        try {
            // 构建多字段查询，配置字段权重
            Query multiMatchQuery = Query.of(q -> q
                .multiMatch(MultiMatchQuery.of(m -> m
                    .query(keyword)
                    .fields("headName^3.0", "headIdCard^2.5", "householdNo^2.5", 
                           "address^1.5", "contactPhone^1.0")
                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                    .fuzziness("AUTO")
                ))
            );

            // 构建搜索请求
            @SuppressWarnings("rawtypes")
            SearchResponse<Map> response = elasticsearchClient.search(
                SearchRequest.of(s -> s
                    .index(ElasticsearchIndexUtil.HOUSEHOLD_INDEX)
                    .query(multiMatchQuery)
                    .from(page * size)
                    .size(size)
                ),
                Map.class
            );

            return buildSearchResult(response);
        } catch (Exception e) {
            log.error("搜索户籍信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0L);
            result.put("hits", new ArrayList<>());
            return result;
        }
    }

    @Override
    public void rebuildIndex() {
        try {
            log.info("开始重建索引");
            // 重建索引结构
            indexUtil.recreateResidentIndex();
            indexUtil.recreateHouseholdIndex();
            log.info("索引重建完成，开始同步数据");

            // 同步居民数据
            syncResidentData();

            // 同步户籍数据
            syncHouseholdData();

            log.info("索引重建和数据同步完成");
        } catch (Exception e) {
            log.error("重建索引失败", e);
            throw new RuntimeException("重建索引失败", e);
        }
    }

    /**
     * 同步居民数据到 Elasticsearch
     */
    private void syncResidentData() {
        try {
            log.info("开始同步居民数据");
            Result<Object> result = residentServiceClient.getAllResidents();
            
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                log.warn("获取居民数据失败或数据为空");
                return;
            }

            // 将数据转换为 List<Map>
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> residents = (List<Map<String, Object>>) result.getData();
            
            if (residents == null || residents.isEmpty()) {
                log.info("没有居民数据需要同步");
                return;
            }

            log.info("获取到 {} 条居民数据，开始批量索引", residents.size());

            // 批量索引文档
            List<BulkOperation> bulkOperations = new ArrayList<>();
            for (Map<String, Object> resident : residents) {
                // 转换日期格式
                convertDateFields(resident);
                
                String id = resident.get("id") != null ? resident.get("id").toString() : null;
                if (id == null) {
                    continue;
                }

                BulkOperation op = BulkOperation.of(o -> o
                    .index(IndexOperation.of(i -> i
                        .index(ElasticsearchIndexUtil.RESIDENT_INDEX)
                        .id(id)
                        .document(resident)
                    ))
                );
                bulkOperations.add(op);
            }

            // 分批处理，每批1000条
            int batchSize = 1000;
            for (int i = 0; i < bulkOperations.size(); i += batchSize) {
                int end = Math.min(i + batchSize, bulkOperations.size());
                List<BulkOperation> batch = bulkOperations.subList(i, end);
                
                BulkRequest bulkRequest = BulkRequest.of(r -> r.operations(batch));
                BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);
                
                if (bulkResponse.errors()) {
                    log.warn("批量索引居民数据时出现错误，批次: {} - {}", i / batchSize + 1, end / batchSize);
                } else {
                    log.info("成功索引居民数据批次: {} - {}，共 {} 条", i / batchSize + 1, end / batchSize, batch.size());
                }
            }

            log.info("居民数据同步完成，共同步 {} 条", residents.size());
        } catch (Exception e) {
            log.error("同步居民数据失败", e);
            throw new RuntimeException("同步居民数据失败", e);
        }
    }

    /**
     * 同步户籍数据到 Elasticsearch
     */
    private void syncHouseholdData() {
        try {
            log.info("开始同步户籍数据");
            Result<Object> result = householdServiceClient.getAllHouseholds();
            
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                log.warn("获取户籍数据失败或数据为空");
                return;
            }

            // 将数据转换为 List<Map>
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> households = (List<Map<String, Object>>) result.getData();
            
            if (households == null || households.isEmpty()) {
                log.info("没有户籍数据需要同步");
                return;
            }

            log.info("获取到 {} 条户籍数据，开始批量索引", households.size());

            // 批量索引文档
            List<BulkOperation> bulkOperations = new ArrayList<>();
            for (Map<String, Object> household : households) {
                // 转换日期格式
                convertDateFields(household);
                
                String id = household.get("id") != null ? household.get("id").toString() : null;
                if (id == null) {
                    continue;
                }

                BulkOperation op = BulkOperation.of(o -> o
                    .index(IndexOperation.of(i -> i
                        .index(ElasticsearchIndexUtil.HOUSEHOLD_INDEX)
                        .id(id)
                        .document(household)
                    ))
                );
                bulkOperations.add(op);
            }

            // 分批处理，每批1000条
            int batchSize = 1000;
            for (int i = 0; i < bulkOperations.size(); i += batchSize) {
                int end = Math.min(i + batchSize, bulkOperations.size());
                List<BulkOperation> batch = bulkOperations.subList(i, end);
                
                BulkRequest bulkRequest = BulkRequest.of(r -> r.operations(batch));
                BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);
                
                if (bulkResponse.errors()) {
                    log.warn("批量索引户籍数据时出现错误，批次: {} - {}", i / batchSize + 1, end / batchSize);
                } else {
                    log.info("成功索引户籍数据批次: {} - {}，共 {} 条", i / batchSize + 1, end / batchSize, batch.size());
                }
            }

            log.info("户籍数据同步完成，共同步 {} 条", households.size());
        } catch (Exception e) {
            log.error("同步户籍数据失败", e);
            throw new RuntimeException("同步户籍数据失败", e);
        }
    }

    /**
     * 转换日期字段格式（将 LocalDate、LocalDateTime 转换为字符串）
     */
    private void convertDateFields(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                String className = value.getClass().getName();
                if (className.startsWith("java.time.")) {
                    // 将日期类型转换为字符串
                    entry.setValue(value.toString());
                } else if (value instanceof Map) {
                    // 递归处理嵌套的 Map
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nestedMap = (Map<String, Object>) value;
                    convertDateFields(nestedMap);
                }
            }
        }
    }

    /**
     * 构建搜索结果
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, Object> buildSearchResult(SearchResponse<Map> response) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> hits = new ArrayList<>();

        if (response.hits().hits() != null) {
            hits = response.hits().hits().stream()
                .map(hit -> {
                    Map<String, Object> source = new HashMap<>();
                    if (hit.source() != null) {
                        source.putAll((Map<? extends String, ?>) hit.source());
                    }
                    source.put("_id", hit.id());
                    if (hit.score() != null) {
                        source.put("_score", hit.score());
                    }
                    return source;
                })
                .collect(Collectors.toList());
        }

        long total = 0L;
        TotalHits totalHits = response.hits().total();
        if (totalHits != null) {
            total = totalHits.value();
        }

        result.put("total", total);
        result.put("hits", hits);
        return result;
    }
}
