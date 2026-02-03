package com.wuzuhao.cpm.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.bulk.DeleteOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.bulk.UpdateOperation;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.search.feign.AdminServiceClient;
import com.wuzuhao.cpm.search.feign.HouseholdMemberServiceClient;
import com.wuzuhao.cpm.search.feign.HouseholdServiceClient;
import com.wuzuhao.cpm.search.feign.ResidentServiceClient;
import com.wuzuhao.cpm.search.feign.UserServiceClient;
import com.wuzuhao.cpm.search.service.ESDocumentSyncService;
import com.wuzuhao.cpm.search.service.SearchService;
import com.wuzuhao.cpm.search.util.ElasticsearchIndexUtil;
import com.wuzuhao.cpm.search.wrapper.ESQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class SearchServiceImpl implements SearchService, ESDocumentSyncService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ElasticsearchIndexUtil indexUtil;

    @Autowired
    private ResidentServiceClient residentServiceClient;

    @Autowired
    private HouseholdServiceClient householdServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private AdminServiceClient adminServiceClient;

    @Autowired
    private HouseholdMemberServiceClient householdMemberServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> searchResident(String keyword, Integer page, Integer size) {
        try {
            // 如果 keyword 为 "*" 或为空，使用 match_all 查询所有数据
            if (keyword == null || keyword.trim().isEmpty() || "*".equals(keyword.trim())) {
                // 构建查询：必须 deleted=0（未删除）
                BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
                boolQueryBuilder.must(Query.of(q -> q.matchAll(co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery.of(m -> m))));
                boolQueryBuilder.must(Query.of(q -> q.term(t -> t
                    .field("deleted")
                    .value(0)
                )));
                Query query = Query.of(q -> q.bool(boolQueryBuilder.build()));
                // 按 id 升序排序
                SortOptions sortOptions = SortOptions.of(s -> s
                    .field(f -> f
                        .field("id")
                        .order(SortOrder.Asc)
                    )
                );
                @SuppressWarnings("rawtypes")
                SearchResponse<Map> response = elasticsearchClient.search(
                    SearchRequest.of(s -> s
                        .index(ElasticsearchIndexUtil.RESIDENT_INDEX)
                        .query(query)
                        .from(page * size)
                        .size(size)
                        .sort(sortOptions)
                    ),
                    Map.class
                );
                return buildSearchResult(response);
            }
            
            // 先尝试完全匹配查询（优先级最高）
            String[] exactMatchFields = {"idCard", "realName.keyword", "contactPhone"};
            Map<String, Object> exactMatchResult = checkExactMatch(
                ElasticsearchIndexUtil.RESIDENT_INDEX, 
                exactMatchFields, 
                keyword
            );
            
            // 如果找到完全匹配的结果，只返回那一条
            if (exactMatchResult != null) {
                return exactMatchResult;
            }
            
            // 如果没有完全匹配，进行模糊匹配查询
            // 构建 BoolQuery：模糊匹配 + deleted=0
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            boolQueryBuilder.must(Query.of(q -> q
                .multiMatch(MultiMatchQuery.of(m -> m
                    .query(keyword.trim())
                    .fields("realName^3.0", "idCard^2.5", "registeredAddress^1.5", 
                           "currentAddress^1.5", "contactPhone^1.0")
                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                    .fuzziness("AUTO")
                ))
            ));
            boolQueryBuilder.must(Query.of(q -> q.term(t -> t
                .field("deleted")
                .value(0)
            )));
            Query fuzzyQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

            // 按 id 升序排序
            SortOptions sortOptions = SortOptions.of(s -> s
                .field(f -> f
                    .field("id")
                    .order(SortOrder.Asc)
                )
            );

            @SuppressWarnings("rawtypes")
            SearchResponse<Map> response = elasticsearchClient.search(
                SearchRequest.of(s -> s
                    .index(ElasticsearchIndexUtil.RESIDENT_INDEX)
                    .query(fuzzyQuery)
                    .from(page * size)
                    .size(size)
                    .sort(sortOptions)
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
            // 如果 keyword 为 "*" 或为空，使用 match_all 查询所有数据
            if (keyword == null || keyword.trim().isEmpty() || "*".equals(keyword.trim())) {
                // 构建查询：必须 deleted=0（未删除）
                BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
                boolQueryBuilder.must(Query.of(q -> q.matchAll(co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery.of(m -> m))));
                boolQueryBuilder.must(Query.of(q -> q.term(t -> t
                    .field("deleted")
                    .value(0)
                )));
                Query query = Query.of(q -> q.bool(boolQueryBuilder.build()));
                // 按 id 升序排序
                SortOptions sortOptions = SortOptions.of(s -> s
                    .field(f -> f
                        .field("id")
                        .order(SortOrder.Asc)
                    )
                );
                @SuppressWarnings("rawtypes")
                SearchResponse<Map> response = elasticsearchClient.search(
                    SearchRequest.of(s -> s
                        .index(ElasticsearchIndexUtil.HOUSEHOLD_INDEX)
                        .query(query)
                        .from(page * size)
                        .size(size)
                        .sort(sortOptions)
                    ),
                    Map.class
                );
                return buildSearchResult(response);
            }
            
            // 先尝试完全匹配查询（优先级最高）
            String[] exactMatchFields = {"headIdCard", "headName.keyword", "householdNo", "contactPhone"};
            Map<String, Object> exactMatchResult = checkExactMatch(
                ElasticsearchIndexUtil.HOUSEHOLD_INDEX, 
                exactMatchFields, 
                keyword
            );
            
            // 如果找到完全匹配的结果，只返回那一条
            if (exactMatchResult != null) {
                return exactMatchResult;
            }
            
            // 如果没有完全匹配，进行模糊匹配查询
            // 构建 BoolQuery：模糊匹配 + deleted=0
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            boolQueryBuilder.must(Query.of(q -> q
                .multiMatch(MultiMatchQuery.of(m -> m
                    .query(keyword.trim())
                    .fields("headName^3.0", "headIdCard^2.5", "householdNo^2.5", 
                           "address^1.5", "contactPhone^1.0")
                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                    .fuzziness("AUTO")
                ))
            ));
            boolQueryBuilder.must(Query.of(q -> q.term(t -> t
                .field("deleted")
                .value(0)
            )));
            Query fuzzyQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

            // 按 id 升序排序
            SortOptions sortOptions = SortOptions.of(s -> s
                .field(f -> f
                    .field("id")
                    .order(SortOrder.Asc)
                )
            );

            @SuppressWarnings("rawtypes")
            SearchResponse<Map> response = elasticsearchClient.search(
                SearchRequest.of(s -> s
                    .index(ElasticsearchIndexUtil.HOUSEHOLD_INDEX)
                    .query(fuzzyQuery)
                    .from(page * size)
                    .size(size)
                    .sort(sortOptions)
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
    public Map<String, Object> searchUser(String keyword, Integer page, Integer size) {
        try {
            // 如果 keyword 为 "*" 或为空，使用 match_all 查询所有数据
            if (keyword == null || keyword.trim().isEmpty() || "*".equals(keyword.trim())) {
                // 构建查询：必须 deleted=0（未删除）
                BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
                boolQueryBuilder.must(Query.of(q -> q.matchAll(co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery.of(m -> m))));
                boolQueryBuilder.must(Query.of(q -> q.term(t -> t
                    .field("deleted")
                    .value(0)
                )));
                Query query = Query.of(q -> q.bool(boolQueryBuilder.build()));
                // 按 id 升序排序
                SortOptions sortOptions = SortOptions.of(s -> s
                    .field(f -> f
                        .field("id")
                        .order(SortOrder.Asc)
                    )
                );
                @SuppressWarnings("rawtypes")
                SearchResponse<Map> response = elasticsearchClient.search(
                    SearchRequest.of(s -> s
                        .index(ElasticsearchIndexUtil.USER_INDEX)
                        .query(query)
                        .from(page * size)
                        .size(size)
                        .sort(sortOptions)
                    ),
                    Map.class
                );
                return buildSearchResult(response);
            }
            
            // 先尝试完全匹配查询（优先级最高）
            String[] exactMatchFields = {"username", "realName.keyword", "phone", "email"};
            Map<String, Object> exactMatchResult = checkExactMatch(
                ElasticsearchIndexUtil.USER_INDEX, 
                exactMatchFields, 
                keyword
            );
            
            // 如果找到完全匹配的结果，只返回那一条
            if (exactMatchResult != null) {
                return exactMatchResult;
            }
            
            // 如果没有完全匹配，进行模糊匹配查询
            // 构建 BoolQuery：模糊匹配 + deleted=0
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            boolQueryBuilder.must(Query.of(q -> q
                .multiMatch(MultiMatchQuery.of(m -> m
                    .query(keyword.trim())
                    .fields("username^3.0", "realName^2.5", "phone^1.5", 
                           "email^1.5")
                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                    .fuzziness("AUTO")
                ))
            ));
            boolQueryBuilder.must(Query.of(q -> q.term(t -> t
                .field("deleted")
                .value(0)
            )));
            Query fuzzyQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));
            
            // 按 id 升序排序
            SortOptions sortOptions = SortOptions.of(s -> s
                .field(f -> f
                    .field("id")
                    .order(SortOrder.Asc)
                )
            );
            
            @SuppressWarnings("rawtypes")
            SearchResponse<Map> response = elasticsearchClient.search(
                SearchRequest.of(s -> s
                    .index(ElasticsearchIndexUtil.USER_INDEX)
                    .query(fuzzyQuery)
                    .from(page * size)
                    .size(size)
                    .sort(sortOptions)
                ),
                Map.class
            );
            
            return buildSearchResult(response);
        } catch (Exception e) {
            log.error("搜索用户信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0L);
            result.put("hits", new ArrayList<>());
            return result;
        }
    }

    @Override
    public Map<String, Object> searchAdmin(String keyword, Integer page, Integer size) {
        try {
            // 如果 keyword 为 "*" 或为空，使用 match_all 查询所有数据
            if (keyword == null || keyword.trim().isEmpty() || "*".equals(keyword.trim())) {
                // 构建查询：必须 deleted=0（未删除）
                BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
                boolQueryBuilder.must(Query.of(q -> q.matchAll(co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery.of(m -> m))));
                boolQueryBuilder.must(Query.of(q -> q.term(t -> t
                    .field("deleted")
                    .value(0)
                )));
                Query query = Query.of(q -> q.bool(boolQueryBuilder.build()));
                // 按 id 升序排序
                SortOptions sortOptions = SortOptions.of(s -> s
                    .field(f -> f
                        .field("id")
                        .order(SortOrder.Asc)
                    )
                );
                @SuppressWarnings("rawtypes")
                SearchResponse<Map> response = elasticsearchClient.search(
                    SearchRequest.of(s -> s
                        .index(ElasticsearchIndexUtil.ADMIN_INDEX)
                        .query(query)
                        .from(page * size)
                        .size(size)
                        .sort(sortOptions)
                    ),
                    Map.class
                );
                return buildSearchResult(response);
            }
            
            // 先尝试完全匹配查询（优先级最高）
            String[] exactMatchFields = {"adminNo.keyword", "department.keyword", "position"};
            Map<String, Object> exactMatchResult = checkExactMatch(
                ElasticsearchIndexUtil.ADMIN_INDEX, 
                exactMatchFields, 
                keyword
            );
            
            // 如果找到完全匹配的结果，只返回那一条
            if (exactMatchResult != null) {
                return exactMatchResult;
            }
            
            // 如果没有完全匹配，进行模糊匹配查询
            // 构建 BoolQuery：模糊匹配 + deleted=0
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            boolQueryBuilder.must(Query.of(q -> q
                .multiMatch(MultiMatchQuery.of(m -> m
                    .query(keyword.trim())
                    .fields("adminNo^3.0", "department^2.5", "position^1.5")
                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                    .fuzziness("AUTO")
                ))
            ));
            boolQueryBuilder.must(Query.of(q -> q.term(t -> t
                .field("deleted")
                .value(0)
            )));
            Query fuzzyQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

            // 按 id 升序排序
            SortOptions sortOptions = SortOptions.of(s -> s
                .field(f -> f
                    .field("id")
                    .order(SortOrder.Asc)
                )
            );
            
            @SuppressWarnings("rawtypes")
            SearchResponse<Map> response = elasticsearchClient.search(
                SearchRequest.of(s -> s
                    .index(ElasticsearchIndexUtil.ADMIN_INDEX)
                    .query(fuzzyQuery)
                    .from(page * size)
                    .size(size)
                    .sort(sortOptions)
                ),
                Map.class
            );
            
            return buildSearchResult(response);
        } catch (Exception e) {
            log.error("搜索管理员信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0L);
            result.put("hits", new ArrayList<>());
            return result;
        }
    }

    @Override
    public Map<String, Object> searchHouseholdMember(String keyword, Integer page, Integer size, Long householdId) {
        try {
            // 构建查询条件
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            
            // 如果指定了 householdId，添加精确匹配条件
            if (householdId != null) {
                boolQueryBuilder.must(Query.of(q -> q
                    .term(TermQuery.of(t -> t
                        .field("householdId")
                        .value(householdId)
                    ))
                ));
            }
            
            // 如果 keyword 为 "*" 或为空，使用 match_all 查询所有数据
            if (keyword == null || keyword.trim().isEmpty() || "*".equals(keyword.trim())) {
                boolQueryBuilder.must(Query.of(q -> q.matchAll(co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery.of(m -> m))));
                Query query = Query.of(q -> q.bool(boolQueryBuilder.build()));
                @SuppressWarnings("rawtypes")
                SearchResponse<Map> response = elasticsearchClient.search(
                    SearchRequest.of(s -> s
                        .index(ElasticsearchIndexUtil.HOUSEHOLD_MEMBER_INDEX)
                        .query(query)
                        .from(page * size)
                        .size(size)
                    ),
                    Map.class
                );
                return buildSearchResult(response);
            }
            
            // 先尝试完全匹配查询（优先级最高）
            // 注意：户籍成员的完全匹配需要考虑householdId条件
            String trimmedKeyword = keyword.trim();
            BoolQuery.Builder exactMatchBuilder = new BoolQuery.Builder();
            
            // 如果指定了 householdId，添加精确匹配条件
            if (householdId != null) {
                exactMatchBuilder.must(Query.of(q -> q
                    .term(TermQuery.of(t -> t
                        .field("householdId")
                        .value(householdId)
                    ))
                ));
            }
            
            // 添加完全匹配字段
            exactMatchBuilder.must(Query.of(q -> q.term(t -> t
                .field("relationship")
                .value(trimmedKeyword)
            )));
            
            Query exactMatchQuery = Query.of(q -> q.bool(exactMatchBuilder.build()));
            
            @SuppressWarnings("rawtypes")
            SearchResponse<Map> exactResponse = elasticsearchClient.search(
                SearchRequest.of(s -> s
                    .index(ElasticsearchIndexUtil.HOUSEHOLD_MEMBER_INDEX)
                    .query(exactMatchQuery)
                    .size(1)  // 只取第一条完全匹配的结果
                ),
                Map.class
            );
            
            // 如果找到完全匹配的结果，只返回那一条
            if (exactResponse.hits().hits() != null && !exactResponse.hits().hits().isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                List<Map<String, Object>> hits = new ArrayList<>();
                
                @SuppressWarnings("rawtypes")
                Hit<Map> hit = exactResponse.hits().hits().get(0);
                Map<String, Object> source = new HashMap<>();
                if (hit.source() != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> sourceMap = (Map<String, Object>) hit.source();
                    source.putAll(sourceMap);
                }
                source.put("_id", hit.id());
                if (hit.score() != null) {
                    source.put("_score", hit.score());
                }
                hits.add(source);
                
                result.put("total", 1L);
                result.put("hits", hits);
                return result;
            }
            
            // 如果没有完全匹配，进行模糊匹配查询
            boolQueryBuilder.must(Query.of(q -> q
                .multiMatch(MultiMatchQuery.of(m -> m
                    .query(trimmedKeyword)
                    .fields("relationship^2.0")
                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                    .fuzziness("AUTO")
                ))
            ));
            
            Query fuzzyQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

            // 构建搜索请求
            @SuppressWarnings("rawtypes")
            SearchResponse<Map> response = elasticsearchClient.search(
                SearchRequest.of(s -> s
                    .index(ElasticsearchIndexUtil.HOUSEHOLD_MEMBER_INDEX)
                    .query(fuzzyQuery)
                    .from(page * size)
                    .size(size)
                ),
                Map.class
            );

            return buildSearchResult(response);
        } catch (Exception e) {
            log.error("搜索户籍成员信息失败", e);
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
            indexUtil.recreateUserIndex();
            indexUtil.recreateAdminIndex();
            indexUtil.recreateHouseholdMemberIndex();
            log.info("索引重建完成，开始同步数据");

            // 同步居民数据
            try {
            syncResidentData();
            } catch (Exception e) {
                log.error("同步居民数据失败，继续执行其他同步", e);
            }

            // 同步户籍数据
            try {
            syncHouseholdData();
            } catch (Exception e) {
                log.error("同步户籍数据失败，继续执行其他同步", e);
            }

            // 同步用户数据
            try {
                syncUserData();
            } catch (Exception e) {
                log.error("同步用户数据失败，继续执行其他同步", e);
            }

            // 同步管理员数据
            try {
                syncAdminData();
            } catch (Exception e) {
                log.error("同步管理员数据失败，继续执行其他同步", e);
            }

            // 同步户籍成员数据
            try {
                syncHouseholdMemberData();
            } catch (Exception e) {
                log.error("同步户籍成员数据失败，继续执行其他同步", e);
            }

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
            Result<?> result = residentServiceClient.getAllResidents();
            
            if (result == null) {
                log.error("获取居民数据失败：Feign调用返回null");
                throw new RuntimeException("获取居民数据失败：Feign调用返回null");
            }
            
            if (result.getCode() != 200) {
                log.error("获取居民数据失败，code: {}, message: {}", result.getCode(), result.getMessage());
                throw new RuntimeException("获取居民数据失败，code: " + result.getCode() + ", message: " + result.getMessage());
            }
            
            if (result.getData() == null) {
                log.warn("获取居民数据为空，可能数据库中没有居民数据");
                return;
            }

            // 将 List<Resident> 转换为 List<Map>
            // Feign 返回的数据可能是 List<Resident> 对象，需要转换为 Map
            Object data = result.getData();
            List<Map<String, Object>> residents = new ArrayList<>();
            
            if (data instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> dataList = (List<Object>) data;
                
                for (Object item : dataList) {
                    try {
                        // 使用 ObjectMapper 将对象转换为 Map
                        @SuppressWarnings("unchecked")
                        Map<String, Object> residentMap = objectMapper.convertValue(item, Map.class);
                        residents.add(residentMap);
                    } catch (Exception e) {
                        log.warn("转换居民对象失败，跳过: {}", e.getMessage());
                        continue;
                    }
                }
            } else {
                log.warn("获取到的居民数据格式不正确，期望 List，实际: {}", 
                    data != null ? data.getClass().getName() : "null");
                return;
            }
            
            if (residents.isEmpty()) {
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
                    log.warn("居民数据缺少ID，跳过: {}", resident);
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
            int successCount = 0;
            int failCount = 0;
            for (int i = 0; i < bulkOperations.size(); i += batchSize) {
                int end = Math.min(i + batchSize, bulkOperations.size());
                List<BulkOperation> batch = bulkOperations.subList(i, end);
                int batchNum = i / batchSize + 1;
                
                BulkRequest bulkRequest = BulkRequest.of(r -> r.operations(batch));
                BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);
                
                if (bulkResponse.errors()) {
                    // 详细记录错误信息
                    log.error("批量索引居民数据时出现错误，批次: {}", batchNum);
                    if (bulkResponse.items() != null) {
                        for (BulkResponseItem item : bulkResponse.items()) {
                            if (item.error() != null) {
                                log.error("索引失败 - ID: {}, 错误类型: {}, 错误原因: {}", 
                                    item.id(), item.error().type(), item.error().reason());
                                failCount++;
                            } else if (item.index() != null) {
                                successCount++;
                            }
                        }
                    }
                } else {
                    successCount += batch.size();
                    log.info("成功索引居民数据批次: {}，共 {} 条", batchNum, batch.size());
                }
            }

            // 刷新索引，使数据立即可搜索
            try {
                elasticsearchClient.indices().refresh(r -> r.index(ElasticsearchIndexUtil.RESIDENT_INDEX));
                log.info("已刷新居民索引");
            } catch (Exception e) {
                log.warn("刷新居民索引失败", e);
            }

            log.info("居民数据同步完成，成功: {} 条，失败: {} 条，总计: {} 条", 
                successCount, failCount, residents.size());
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
            Result<?> result = householdServiceClient.getAllHouseholds();
            
            if (result == null) {
                log.error("获取户籍数据失败：Feign调用返回null");
                throw new RuntimeException("获取户籍数据失败：Feign调用返回null");
            }
            
            if (result.getCode() != 200) {
                log.error("获取户籍数据失败，code: {}, message: {}", result.getCode(), result.getMessage());
                throw new RuntimeException("获取户籍数据失败，code: " + result.getCode() + ", message: " + result.getMessage());
            }
            
            if (result.getData() == null) {
                log.warn("获取户籍数据为空，可能数据库中没有户籍数据");
                return;
            }

            // 将 List<Household> 转换为 List<Map>
            // Feign 返回的数据可能是 List<Household> 对象，需要转换为 Map
            Object data = result.getData();
            List<Map<String, Object>> households = new ArrayList<>();
            
            if (data instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> dataList = (List<Object>) data;
                
                for (Object item : dataList) {
                    try {
                        // 使用 ObjectMapper 将对象转换为 Map
                        @SuppressWarnings("unchecked")
                        Map<String, Object> householdMap = objectMapper.convertValue(item, Map.class);
                        households.add(householdMap);
                    } catch (Exception e) {
                        log.warn("转换户籍对象失败，跳过: {}", e.getMessage());
                        continue;
                    }
                }
            } else {
                log.warn("获取到的户籍数据格式不正确，期望 List，实际: {}", 
                    data != null ? data.getClass().getName() : "null");
                return;
            }
            
            if (households.isEmpty()) {
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
                    log.warn("户籍数据缺少ID，跳过: {}", household);
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
            int successCount = 0;
            int failCount = 0;
            for (int i = 0; i < bulkOperations.size(); i += batchSize) {
                int end = Math.min(i + batchSize, bulkOperations.size());
                List<BulkOperation> batch = bulkOperations.subList(i, end);
                int batchNum = i / batchSize + 1;
                
                BulkRequest bulkRequest = BulkRequest.of(r -> r.operations(batch));
                BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);
                
                if (bulkResponse.errors()) {
                    // 详细记录错误信息
                    log.error("批量索引户籍数据时出现错误，批次: {}", batchNum);
                    if (bulkResponse.items() != null) {
                        for (BulkResponseItem item : bulkResponse.items()) {
                            if (item.error() != null) {
                                log.error("索引失败 - ID: {}, 错误类型: {}, 错误原因: {}", 
                                    item.id(), item.error().type(), item.error().reason());
                                failCount++;
                            } else if (item.index() != null) {
                                successCount++;
                            }
                        }
                    }
                } else {
                    successCount += batch.size();
                    log.info("成功索引户籍数据批次: {}，共 {} 条", batchNum, batch.size());
                }
            }

            // 刷新索引，使数据立即可搜索
            try {
                elasticsearchClient.indices().refresh(r -> r.index(ElasticsearchIndexUtil.HOUSEHOLD_INDEX));
                log.info("已刷新户籍索引");
            } catch (Exception e) {
                log.warn("刷新户籍索引失败", e);
            }

            log.info("户籍数据同步完成，成功: {} 条，失败: {} 条，总计: {} 条", 
                successCount, failCount, households.size());
        } catch (Exception e) {
            log.error("同步户籍数据失败", e);
            throw new RuntimeException("同步户籍数据失败", e);
        }
    }

    /**
     * 同步用户数据到 Elasticsearch
     */
    private void syncUserData() {
        try {
            log.info("开始同步用户数据");
            Result<?> result = userServiceClient.getAllUsers();
            
            if (result == null) {
                log.error("获取用户数据失败：Feign调用返回null");
                throw new RuntimeException("获取用户数据失败：Feign调用返回null");
            }
            
            if (result.getCode() != 200) {
                log.error("获取用户数据失败，code: {}, message: {}", result.getCode(), result.getMessage());
                throw new RuntimeException("获取用户数据失败，code: " + result.getCode() + ", message: " + result.getMessage());
            }
            
            if (result.getData() == null) {
                log.warn("获取用户数据为空，可能数据库中没有用户数据");
                return;
            }

            // 将 List<User> 转换为 List<Map>
            Object data = result.getData();
            List<Map<String, Object>> users = new ArrayList<>();
            
            if (data instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> dataList = (List<Object>) data;
                
                for (Object item : dataList) {
                    try {
                        // 使用 ObjectMapper 将对象转换为 Map
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userMap = objectMapper.convertValue(item, Map.class);
                        // 移除密码字段，不索引密码
                        userMap.remove("password");
                        users.add(userMap);
                    } catch (Exception e) {
                        log.warn("转换用户对象失败，跳过: {}", e.getMessage());
                        continue;
                    }
                }
            } else {
                log.warn("获取到的用户数据格式不正确，期望 List，实际: {}", 
                    data != null ? data.getClass().getName() : "null");
                return;
            }
            
            if (users.isEmpty()) {
                log.info("没有用户数据需要同步");
                return;
            }

            log.info("获取到 {} 条用户数据，开始批量索引", users.size());

            // 批量索引文档
            List<BulkOperation> bulkOperations = new ArrayList<>();
            for (Map<String, Object> user : users) {
                // 转换日期格式
                convertDateFields(user);
                
                String id = user.get("id") != null ? user.get("id").toString() : null;
                if (id == null) {
                    log.warn("用户数据缺少ID，跳过: {}", user);
                    continue;
                }

                BulkOperation op = BulkOperation.of(o -> o
                    .index(IndexOperation.of(i -> i
                        .index(ElasticsearchIndexUtil.USER_INDEX)
                        .id(id)
                        .document(user)
                    ))
                );
                bulkOperations.add(op);
            }

            // 分批处理，每批1000条
            int batchSize = 1000;
            int successCount = 0;
            int failCount = 0;
            for (int i = 0; i < bulkOperations.size(); i += batchSize) {
                int end = Math.min(i + batchSize, bulkOperations.size());
                List<BulkOperation> batch = bulkOperations.subList(i, end);
                int batchNum = i / batchSize + 1;
                
                BulkRequest bulkRequest = BulkRequest.of(r -> r.operations(batch));
                BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);
                
                if (bulkResponse.errors()) {
                    // 详细记录错误信息
                    log.error("批量索引用户数据时出现错误，批次: {}", batchNum);
                    if (bulkResponse.items() != null) {
                        for (BulkResponseItem item : bulkResponse.items()) {
                            if (item.error() != null) {
                                log.error("索引失败 - ID: {}, 错误类型: {}, 错误原因: {}", 
                                    item.id(), item.error().type(), item.error().reason());
                                failCount++;
                            } else if (item.index() != null) {
                                successCount++;
                            }
                        }
                    }
                } else {
                    successCount += batch.size();
                    log.info("成功索引用户数据批次: {}，共 {} 条", batchNum, batch.size());
                }
            }

            // 刷新索引，使数据立即可搜索
            try {
                elasticsearchClient.indices().refresh(r -> r.index(ElasticsearchIndexUtil.USER_INDEX));
                log.info("已刷新用户索引");
            } catch (Exception e) {
                log.warn("刷新用户索引失败", e);
            }

            log.info("用户数据同步完成，成功: {} 条，失败: {} 条，总计: {} 条", 
                successCount, failCount, users.size());
        } catch (Exception e) {
            log.error("同步用户数据失败", e);
            throw new RuntimeException("同步用户数据失败", e);
        }
    }

    /**
     * 同步管理员数据到 Elasticsearch
     */
    private void syncAdminData() {
        try {
            log.info("开始同步管理员数据");
            Result<?> result = adminServiceClient.getAllAdmins();
            
            if (result == null) {
                log.error("获取管理员数据失败：Feign调用返回null");
                throw new RuntimeException("获取管理员数据失败：Feign调用返回null");
            }
            
            if (result.getCode() != 200) {
                log.error("获取管理员数据失败，code: {}, message: {}", result.getCode(), result.getMessage());
                throw new RuntimeException("获取管理员数据失败，code: " + result.getCode() + ", message: " + result.getMessage());
            }
            
            if (result.getData() == null) {
                log.warn("获取管理员数据为空，可能数据库中没有管理员数据");
                return;
            }

            // 将 List<Admin> 转换为 List<Map>
            Object data = result.getData();
            List<Map<String, Object>> admins = new ArrayList<>();
            
            if (data instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> dataList = (List<Object>) data;
                
                for (Object item : dataList) {
                    try {
                        // 使用 ObjectMapper 将对象转换为 Map
                        @SuppressWarnings("unchecked")
                        Map<String, Object> adminMap = objectMapper.convertValue(item, Map.class);
                        admins.add(adminMap);
                    } catch (Exception e) {
                        log.warn("转换管理员对象失败，跳过: {}", e.getMessage());
                        continue;
                    }
                }
            } else {
                log.warn("获取到的管理员数据格式不正确，期望 List，实际: {}", 
                    data != null ? data.getClass().getName() : "null");
                return;
            }
            
            if (admins.isEmpty()) {
                log.info("没有管理员数据需要同步");
                return;
            }

            log.info("获取到 {} 条管理员数据，开始批量索引", admins.size());

            // 批量索引文档
            List<BulkOperation> bulkOperations = new ArrayList<>();
            for (Map<String, Object> admin : admins) {
                // 转换日期格式
                convertDateFields(admin);
                
                String id = admin.get("id") != null ? admin.get("id").toString() : null;
                if (id == null) {
                    log.warn("管理员数据缺少ID，跳过: {}", admin);
                    continue;
                }

                BulkOperation op = BulkOperation.of(o -> o
                    .index(IndexOperation.of(i -> i
                        .index(ElasticsearchIndexUtil.ADMIN_INDEX)
                        .id(id)
                        .document(admin)
                    ))
                );
                bulkOperations.add(op);
            }

            // 分批处理，每批1000条
            int batchSize = 1000;
            int successCount = 0;
            int failCount = 0;
            for (int i = 0; i < bulkOperations.size(); i += batchSize) {
                int end = Math.min(i + batchSize, bulkOperations.size());
                List<BulkOperation> batch = bulkOperations.subList(i, end);
                int batchNum = i / batchSize + 1;
                
                BulkRequest bulkRequest = BulkRequest.of(r -> r.operations(batch));
                BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);
                
                if (bulkResponse.errors()) {
                    // 详细记录错误信息
                    log.error("批量索引管理员数据时出现错误，批次: {}", batchNum);
                    if (bulkResponse.items() != null) {
                        for (BulkResponseItem item : bulkResponse.items()) {
                            if (item.error() != null) {
                                log.error("索引失败 - ID: {}, 错误类型: {}, 错误原因: {}", 
                                    item.id(), item.error().type(), item.error().reason());
                                failCount++;
                            } else if (item.index() != null) {
                                successCount++;
                            }
                        }
                    }
                } else {
                    successCount += batch.size();
                    log.info("成功索引管理员数据批次: {}，共 {} 条", batchNum, batch.size());
                }
            }

            // 刷新索引，使数据立即可搜索
            try {
                elasticsearchClient.indices().refresh(r -> r.index(ElasticsearchIndexUtil.ADMIN_INDEX));
                log.info("已刷新管理员索引");
            } catch (Exception e) {
                log.warn("刷新管理员索引失败", e);
            }

            log.info("管理员数据同步完成，成功: {} 条，失败: {} 条，总计: {} 条", 
                successCount, failCount, admins.size());
        } catch (Exception e) {
            log.error("同步管理员数据失败", e);
            throw new RuntimeException("同步管理员数据失败", e);
        }
    }

    /**
     * 同步户籍成员数据到 Elasticsearch
     */
    private void syncHouseholdMemberData() {
        try {
            log.info("开始同步户籍成员数据");
            Result<?> result = householdMemberServiceClient.getAllHouseholdMembers();
            
            if (result == null) {
                log.error("获取户籍成员数据失败：Feign调用返回null");
                throw new RuntimeException("获取户籍成员数据失败：Feign调用返回null");
            }
            
            if (result.getCode() != 200) {
                log.error("获取户籍成员数据失败，code: {}, message: {}", result.getCode(), result.getMessage());
                throw new RuntimeException("获取户籍成员数据失败，code: " + result.getCode() + ", message: " + result.getMessage());
            }
            
            if (result.getData() == null) {
                log.warn("获取户籍成员数据为空，可能数据库中没有户籍成员数据");
                return;
            }

            // 将 List<HouseholdMember> 转换为 List<Map>
            Object data = result.getData();
            List<Map<String, Object>> members = new ArrayList<>();
            
            if (data instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> dataList = (List<Object>) data;
                
                for (Object item : dataList) {
                    try {
                        // 使用 ObjectMapper 将对象转换为 Map
                        @SuppressWarnings("unchecked")
                        Map<String, Object> memberMap = objectMapper.convertValue(item, Map.class);
                        members.add(memberMap);
                    } catch (Exception e) {
                        log.warn("转换户籍成员对象失败，跳过: {}", e.getMessage());
                        continue;
                    }
                }
            } else {
                log.warn("获取到的户籍成员数据格式不正确，期望 List，实际: {}", 
                    data != null ? data.getClass().getName() : "null");
                return;
            }
            
            if (members.isEmpty()) {
                log.info("没有户籍成员数据需要同步");
                return;
            }

            log.info("获取到 {} 条户籍成员数据，开始批量索引", members.size());

            // 批量索引文档
            List<BulkOperation> bulkOperations = new ArrayList<>();
            for (Map<String, Object> member : members) {
                // 转换日期格式
                convertDateFields(member);
                
                String id = member.get("id") != null ? member.get("id").toString() : null;
                if (id == null) {
                    log.warn("户籍成员数据缺少ID，跳过: {}", member);
                    continue;
                }

                BulkOperation op = BulkOperation.of(o -> o
                    .index(IndexOperation.of(i -> i
                        .index(ElasticsearchIndexUtil.HOUSEHOLD_MEMBER_INDEX)
                        .id(id)
                        .document(member)
                    ))
                );
                bulkOperations.add(op);
            }

            // 分批处理，每批1000条
            int batchSize = 1000;
            int successCount = 0;
            int failCount = 0;
            for (int i = 0; i < bulkOperations.size(); i += batchSize) {
                int end = Math.min(i + batchSize, bulkOperations.size());
                List<BulkOperation> batch = bulkOperations.subList(i, end);
                int batchNum = i / batchSize + 1;
                
                BulkRequest bulkRequest = BulkRequest.of(r -> r.operations(batch));
                BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);
                
                if (bulkResponse.errors()) {
                    // 详细记录错误信息
                    log.error("批量索引户籍成员数据时出现错误，批次: {}", batchNum);
                    if (bulkResponse.items() != null) {
                        for (BulkResponseItem item : bulkResponse.items()) {
                            if (item.error() != null) {
                                log.error("索引失败 - ID: {}, 错误类型: {}, 错误原因: {}", 
                                    item.id(), item.error().type(), item.error().reason());
                                failCount++;
                            } else if (item.index() != null) {
                                successCount++;
                            }
                        }
                    }
                } else {
                    successCount += batch.size();
                    log.info("成功索引户籍成员数据批次: {}，共 {} 条", batchNum, batch.size());
                }
            }

            // 刷新索引，使数据立即可搜索
            try {
                elasticsearchClient.indices().refresh(r -> r.index(ElasticsearchIndexUtil.HOUSEHOLD_MEMBER_INDEX));
                log.info("已刷新户籍成员索引");
            } catch (Exception e) {
                log.warn("刷新户籍成员索引失败", e);
            }

            log.info("户籍成员数据同步完成，成功: {} 条，失败: {} 条，总计: {} 条", 
                successCount, failCount, members.size());
        } catch (Exception e) {
            log.error("同步户籍成员数据失败", e);
            throw new RuntimeException("同步户籍成员数据失败", e);
        }
    }

    /**
     * 转换日期字段格式（将 LocalDate、LocalDateTime 转换为字符串）
     */
    private void convertDateFields(Map<String, Object> map) {
        // 日期时间格式，用于解析带时间的字符串
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                String className = value.getClass().getName();
                if (className.startsWith("java.time.")) {
                    // 处理 Java 8 时间类型
                    if (value instanceof LocalDateTime) {
                        LocalDateTime dateTime = (LocalDateTime) value;
                        // 转换为包含时间的格式，以便前端正确显示
                        entry.setValue(dateTime.format(dateTimeFormatter));
                    } else if (value instanceof LocalDate) {
                        LocalDate date = (LocalDate) value;
                        // LocalDate 只包含日期，使用日期格式
                        entry.setValue(date.format(dateFormatter));
                    } else {
                        // 其他时间类型，转换为字符串
                        entry.setValue(value.toString());
                    }
                } else if (value instanceof String) {
                    // 处理字符串格式的日期（如 "2022-11-13 00:00:00"）
                    String strValue = (String) value;
                    String fieldName = entry.getKey();
                    // 检查是否是日期字段
                    if (fieldName.contains("Date") || fieldName.contains("Time")) {
                        try {
                            // birthDate 字段特殊处理：只保留日期部分，不包含时间
                            if ("birthDate".equals(fieldName)) {
                                if (strValue.contains(" ")) {
                                    // 如果包含时间部分，只提取日期部分
                                    String datePart = strValue.substring(0, 10);
                                    if (datePart.matches("\\d{4}-\\d{2}-\\d{2}")) {
                                        entry.setValue(datePart);
                                    }
                                } else if (strValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
                                    // 已经是日期格式，保持不变
                                    // entry.setValue 保持不变
                                }
                            } else {
                                // 其他日期时间字段（createTime、updateTime 等）
                                // 如果已经包含时间部分（"yyyy-MM-dd HH:mm:ss"），保持原值
                                if (strValue.contains(" ") && strValue.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                                    // 已经是完整的时间格式，保持不变，以便前端正确显示
                                    // entry.setValue 保持不变
                                } else if (strValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
                                    // 只有日期格式（"yyyy-MM-dd"），转换为完整时间格式（"yyyy-MM-dd 00:00:00"）
                                    // 以便与前端期望的格式保持一致
                                    LocalDate date = LocalDate.parse(strValue, dateFormatter);
                                    entry.setValue(date.atStartOfDay().format(dateTimeFormatter));
                                }
                            }
                        } catch (Exception e) {
                            // 解析失败，保持原值
                            log.debug("日期字段 {} 解析失败，保持原值: {}", fieldName, strValue);
                        }
                    }
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
                    // 统一转换日期格式为 "yyyy-MM-dd HH:mm:ss"，确保前端正确显示
                    convertDateFields(source);
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

    /**
     * 检查完全匹配并返回结果（如果存在）
     * @param index 索引名称
     * @param exactMatchFields 完全匹配的字段列表（字段名数组）
     * @param keyword 搜索关键词
     * @return 如果找到完全匹配，返回结果；否则返回null
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, Object> checkExactMatch(String index, String[] exactMatchFields, String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return null;
            }
            
            String trimmedKeyword = keyword.trim();
            BoolQuery.Builder exactMatchBuilder = new BoolQuery.Builder();
            
            // 为每个字段添加完全匹配查询
            for (String field : exactMatchFields) {
                exactMatchBuilder.should(Query.of(q -> q.term(t -> t
                    .field(field)
                    .value(trimmedKeyword)
                )));
            }
            
            // 添加 deleted=0 的过滤条件（仅对用户索引）
            if (ElasticsearchIndexUtil.USER_INDEX.equals(index) || 
                ElasticsearchIndexUtil.RESIDENT_INDEX.equals(index) ||
                ElasticsearchIndexUtil.HOUSEHOLD_INDEX.equals(index)) {
                exactMatchBuilder.must(Query.of(q -> q.term(t -> t
                    .field("deleted")
                    .value(0)
                )));
            }
            
            Query exactMatchQuery = Query.of(q -> q.bool(exactMatchBuilder.build()));
            
            // 按 id 升序排序，确保返回 id 最小的完全匹配结果
            SortOptions sortOptions = SortOptions.of(s -> s
                .field(f -> f
                    .field("id")
                    .order(SortOrder.Asc)
                )
            );
            
            SearchResponse<Map> exactResponse = elasticsearchClient.search(
                SearchRequest.of(s -> s
                    .index(index)
                    .query(exactMatchQuery)
                    .size(1)  // 只取第一条完全匹配的结果
                    .sort(sortOptions)
                ),
                Map.class
            );
            
            // 如果找到完全匹配的结果，只返回那一条
            if (exactResponse.hits().hits() != null && !exactResponse.hits().hits().isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                List<Map<String, Object>> hits = new ArrayList<>();
                
                Hit<Map> hit = exactResponse.hits().hits().get(0);
                Map<String, Object> source = new HashMap<>();
                if (hit.source() != null) {
                    source.putAll((Map<? extends String, ?>) hit.source());
                }
                source.put("_id", hit.id());
                if (hit.score() != null) {
                    source.put("_score", hit.score());
                }
                hits.add(source);
                
                result.put("total", 1L);
                result.put("hits", hits);
                return result;
            }
        } catch (Exception e) {
            log.debug("完全匹配查询失败，继续使用模糊匹配: {}", e.getMessage());
        }
        
        return null;
    }
    
    // ========== 查询操作实现 ==========
    
    @Override
    public Map<String, Object> getById(String index, Long id) {
        try {
            GetResponse<Map> response = elasticsearchClient.get(g -> g
                .index(index)
                .id(id.toString()),
                Map.class
            );
            
            if (response.found()) {
                Map<String, Object> result = new HashMap<>();
                if (response.source() != null) {
                    result.putAll(response.source());
                }
                result.put("_id", response.id());
                return result;
            }
            return null;
        } catch (Exception e) {
            log.error("按ID查询文档失败，index: {}, id: {}", index, id, e);
            return null;
        }
    }
    
    // ========== ES文档同步操作实现（内部使用，不对外暴露） ==========
    
    @Override
    public String saveDocument(String index, Map<String, Object> document) {
        try {
            // 转换日期字段格式
            convertDateFields(document);
            
            Object idObj = document.get("id");
            String id = idObj != null ? idObj.toString() : null;
            
            IndexResponse response;
            if (id != null) {
                response = elasticsearchClient.index(i -> i
                    .index(index)
                    .id(id)
                    .document(document)
                );
            } else {
                response = elasticsearchClient.index(i -> i
                    .index(index)
                    .document(document)
                );
            }
            
            log.debug("创建文档成功，index: {}, id: {}", index, response.id());
            return response.id();
        } catch (Exception e) {
            log.error("创建文档失败，index: {}", index, e);
            throw new RuntimeException("创建文档失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean updateDocument(String index, Long id, Map<String, Object> document) {
        try {
            // 转换日期字段格式
            convertDateFields(document);
            
            elasticsearchClient.update(u -> u
                .index(index)
                .id(id.toString())
                .doc(document),
                Map.class
            );
            
            log.debug("更新文档成功，index: {}, id: {}", index, id);
            return true;
        } catch (Exception e) {
            log.error("更新文档失败，index: {}, id: {}", index, id, e);
            return false;
        }
    }
    
    @Override
    public boolean removeDocument(String index, Long id) {
        try {
            elasticsearchClient.delete(d -> d
                .index(index)
                .id(id.toString())
            );
            
            log.info("删除文档成功，index: {}, id: {}", index, id);
            return true;
        } catch (Exception e) {
            log.error("删除文档失败，index: {}, id: {}", index, id, e);
            return false;
        }
    }
    
    // ========== 批量操作实现 ==========
    
    @Override
    public int saveBatchDocuments(String index, List<Map<String, Object>> documents) {
        if (documents == null || documents.isEmpty()) {
            return 0;
        }
        
        try {
            List<BulkOperation> bulkOperations = new ArrayList<>();
            for (Map<String, Object> doc : documents) {
                // 转换日期字段格式
                convertDateFields(doc);
                
                Object idObj = doc.get("id");
                String id = idObj != null ? idObj.toString() : null;
                
                if (id != null) {
                    bulkOperations.add(BulkOperation.of(o -> o
                        .index(IndexOperation.of(i -> i
                            .index(index)
                            .id(id)
                            .document(doc)
                        ))
                    ));
                } else {
                    bulkOperations.add(BulkOperation.of(o -> o
                        .index(IndexOperation.of(i -> i
                            .index(index)
                            .document(doc)
                        ))
                    ));
                }
            }
            
            BulkRequest bulkRequest = BulkRequest.of(r -> r.operations(bulkOperations));
            BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);
            
            int successCount = 0;
            if (bulkResponse.items() != null) {
                for (BulkResponseItem item : bulkResponse.items()) {
                    if (item.error() == null) {
                        successCount++;
                    } else {
                        log.error("批量创建文档失败，index: {}, id: {}, 错误: {}", 
                            index, item.id(), item.error().reason());
                    }
                }
            }
            
            log.info("批量创建文档完成，index: {}, 成功: {}/{}", index, successCount, documents.size());
            return successCount;
        } catch (Exception e) {
            log.error("批量创建文档失败，index: {}", index, e);
            throw new RuntimeException("批量创建文档失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int updateBatchDocuments(String index, List<Map<String, Object>> documents) {
        if (documents == null || documents.isEmpty()) {
            return 0;
        }
        
        try {
            List<BulkOperation> bulkOperations = new ArrayList<>();
            for (Map<String, Object> doc : documents) {
                // 转换日期字段格式
                convertDateFields(doc);
                
                Object idObj = doc.get("id");
                if (idObj == null) {
                    log.warn("批量更新文档时缺少id字段，跳过该文档");
                    continue;
                }
                String id = idObj.toString();
                
                bulkOperations.add(BulkOperation.of(o -> o
                    .update(UpdateOperation.of(u -> u
                        .index(index)
                        .id(id)
                        .action(a -> a.doc(doc))
                    ))
                ));
            }
            
            if (bulkOperations.isEmpty()) {
                return 0;
            }
            
            BulkRequest bulkRequest = BulkRequest.of(r -> r.operations(bulkOperations));
            BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);
            
            int successCount = 0;
            if (bulkResponse.items() != null) {
                for (BulkResponseItem item : bulkResponse.items()) {
                    if (item.error() == null) {
                        successCount++;
                    } else {
                        log.error("批量更新文档失败，index: {}, id: {}, 错误: {}", 
                            index, item.id(), item.error().reason());
                    }
                }
            }
            
            log.info("批量更新文档完成，index: {}, 成功: {}/{}", index, successCount, documents.size());
            return successCount;
        } catch (Exception e) {
            log.error("批量更新文档失败，index: {}", index, e);
            throw new RuntimeException("批量更新文档失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int removeBatchDocuments(String index, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        try {
            List<BulkOperation> bulkOperations = new ArrayList<>();
            for (Long id : ids) {
                bulkOperations.add(BulkOperation.of(o -> o
                    .delete(DeleteOperation.of(d -> d
                        .index(index)
                        .id(id.toString())
                    ))
                ));
            }
            
            BulkRequest bulkRequest = BulkRequest.of(r -> r.operations(bulkOperations));
            BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);
            
            int successCount = 0;
            if (bulkResponse.items() != null) {
                for (BulkResponseItem item : bulkResponse.items()) {
                    if (item.error() == null) {
                        successCount++;
                    } else {
                        log.error("批量删除文档失败，index: {}, id: {}, 错误: {}", 
                            index, item.id(), item.error().reason());
                    }
                }
            }
            
            log.info("批量删除文档完成，index: {}, 成功: {}/{}", index, successCount, ids.size());
            return successCount;
        } catch (Exception e) {
            log.error("批量删除文档失败，index: {}", index, e);
            throw new RuntimeException("批量删除文档失败: " + e.getMessage(), e);
        }
    }
    
    // ========== 条件查询实现 ==========
    
    @Override
    public List<Map<String, Object>> list(String index, ESQueryWrapper wrapper) {
        try {
            Query query = wrapper.build();
            SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                .index(index)
                .query(query);
            
            // 添加排序
            if (wrapper.hasSort()) {
                List<co.elastic.clients.elasticsearch._types.SortOptions> sortOptions = new ArrayList<>();
                for (com.wuzuhao.cpm.search.wrapper.ESQueryWrapper.SortField sortField : wrapper.getSortFields()) {
                    // 使用 SortOptions 直接构建排序
                    co.elastic.clients.elasticsearch._types.SortOptions.Builder sortBuilder = 
                        new co.elastic.clients.elasticsearch._types.SortOptions.Builder();
                    sortBuilder.field(f -> f
                        .field(sortField.getField())
                        .order(sortField.getOrder())
                    );
                    sortOptions.add(sortBuilder.build());
                }
                requestBuilder.sort(sortOptions);
            }
            
            // 设置最大返回数量
            requestBuilder.size(10000); // ES 默认最大 10000
            
            @SuppressWarnings("rawtypes")
            SearchResponse<Map> response = elasticsearchClient.search(
                requestBuilder.build(),
                Map.class
            );
            
            List<Map<String, Object>> result = new ArrayList<>();
            if (response.hits().hits() != null) {
                for (@SuppressWarnings("rawtypes") Hit<Map> hit : response.hits().hits()) {
                    Map<String, Object> source = new HashMap<>();
                    if (hit.source() != null) {
                        source.putAll((Map<? extends String, ?>) hit.source());
                    }
                    source.put("_id", hit.id());
                    if (hit.score() != null) {
                        source.put("_score", hit.score());
                    }
                    result.add(source);
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("条件查询列表失败，index: {}", index, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> page(String index, ESQueryWrapper wrapper, Integer current, Integer size) {
        try {
            Query query = wrapper.build();
            int from = (current - 1) * size;
            
            SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                .index(index)
                .query(query)
                .from(from)
                .size(size);
            
            // 添加排序
            if (wrapper.hasSort()) {
                List<co.elastic.clients.elasticsearch._types.SortOptions> sortOptions = new ArrayList<>();
                for (com.wuzuhao.cpm.search.wrapper.ESQueryWrapper.SortField sortField : wrapper.getSortFields()) {
                    // 使用 SortOptions 直接构建排序
                    co.elastic.clients.elasticsearch._types.SortOptions.Builder sortBuilder = 
                        new co.elastic.clients.elasticsearch._types.SortOptions.Builder();
                    sortBuilder.field(f -> f
                        .field(sortField.getField())
                        .order(sortField.getOrder())
                    );
                    sortOptions.add(sortBuilder.build());
                }
                requestBuilder.sort(sortOptions);
            }
            
            @SuppressWarnings("rawtypes")
            SearchResponse<Map> response = elasticsearchClient.search(
                requestBuilder.build(),
                Map.class
            );
            
            return buildSearchResult(response);
        } catch (Exception e) {
            log.error("条件分页查询失败，index: {}, current: {}, size: {}", index, current, size, e);
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0L);
            result.put("hits", new ArrayList<>());
            return result;
        }
    }
}
