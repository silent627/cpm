package com.wuzuhao.cpm.search.wrapper;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.json.JsonData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Elasticsearch 查询构建器
 * 类似 MyBatis-Plus 的 LambdaQueryWrapper，提供链式查询构建
 */
@Data
public class ESQueryWrapper {
    
    private BoolQuery.Builder boolQueryBuilder;
    private List<SortField> sortFields;
    
    public ESQueryWrapper() {
        this.boolQueryBuilder = new BoolQuery.Builder();
        this.sortFields = new ArrayList<>();
    }
    
    /**
     * 排序字段内部类
     */
    @Data
    public static class SortField {
        private String field;
        private SortOrder order;
        
        public SortField(String field, SortOrder order) {
            this.field = field;
            this.order = order;
        }
    }
    
    /**
     * 等于查询
     */
    public ESQueryWrapper eq(String field, Object value) {
        if (value != null) {
            boolQueryBuilder.must(Query.of(q -> q
                .term(TermQuery.of(t -> t
                    .field(field)
                    .value(value.toString())
                ))
            ));
        }
        return this;
    }
    
    /**
     * 不等于查询
     */
    public ESQueryWrapper ne(String field, Object value) {
        if (value != null) {
            boolQueryBuilder.mustNot(Query.of(q -> q
                .term(TermQuery.of(t -> t
                    .field(field)
                    .value(value.toString())
                ))
            ));
        }
        return this;
    }
    
    /**
     * 模糊查询（使用 match 查询）
     */
    public ESQueryWrapper like(String field, String value) {
        if (value != null && !value.trim().isEmpty()) {
            boolQueryBuilder.must(Query.of(q -> q
                .match(m -> m
                    .field(field)
                    .query(value.trim())
                )
            ));
        }
        return this;
    }
    
    /**
     * IN 查询
     */
    public ESQueryWrapper in(String field, List<Object> values) {
        if (values != null && !values.isEmpty()) {
            BoolQuery.Builder inBuilder = new BoolQuery.Builder();
            for (Object value : values) {
                if (value != null) {
                    inBuilder.should(Query.of(q -> q
                        .term(TermQuery.of(t -> t
                            .field(field)
                            .value(value.toString())
                        ))
                    ));
                }
            }
            boolQueryBuilder.must(Query.of(q -> q.bool(inBuilder.build())));
        }
        return this;
    }
    
    /**
     * 范围查询（between）
     */
    public ESQueryWrapper between(String field, Object start, Object end) {
        if (start != null || end != null) {
            RangeQuery.Builder rangeBuilder = new RangeQuery.Builder().field(field);
            if (start != null) {
                rangeBuilder.gte(JsonData.of(start.toString()));
            }
            if (end != null) {
                rangeBuilder.lte(JsonData.of(end.toString()));
            }
            boolQueryBuilder.must(Query.of(q -> q.range(rangeBuilder.build())));
        }
        return this;
    }
    
    /**
     * 大于查询
     */
    public ESQueryWrapper gt(String field, Object value) {
        if (value != null) {
            boolQueryBuilder.must(Query.of(q -> q
                .range(RangeQuery.of(r -> r
                    .field(field)
                    .gt(JsonData.of(value.toString()))
                ))
            ));
        }
        return this;
    }
    
    /**
     * 大于等于查询
     */
    public ESQueryWrapper ge(String field, Object value) {
        if (value != null) {
            boolQueryBuilder.must(Query.of(q -> q
                .range(RangeQuery.of(r -> r
                    .field(field)
                    .gte(JsonData.of(value.toString()))
                ))
            ));
        }
        return this;
    }
    
    /**
     * 小于查询
     */
    public ESQueryWrapper lt(String field, Object value) {
        if (value != null) {
            boolQueryBuilder.must(Query.of(q -> q
                .range(RangeQuery.of(r -> r
                    .field(field)
                    .lt(JsonData.of(value.toString()))
                ))
            ));
        }
        return this;
    }
    
    /**
     * 小于等于查询
     */
    public ESQueryWrapper le(String field, Object value) {
        if (value != null) {
            boolQueryBuilder.must(Query.of(q -> q
                .range(RangeQuery.of(r -> r
                    .field(field)
                    .lte(JsonData.of(value.toString()))
                ))
            ));
        }
        return this;
    }
    
    /**
     * 升序排序
     */
    public ESQueryWrapper orderByAsc(String field) {
        sortFields.add(new SortField(field, SortOrder.Asc));
        return this;
    }
    
    /**
     * 降序排序
     */
    public ESQueryWrapper orderByDesc(String field) {
        sortFields.add(new SortField(field, SortOrder.Desc));
        return this;
    }
    
    /**
     * 转换为 ES Query 对象
     */
    public Query build() {
        BoolQuery boolQuery = boolQueryBuilder.build();
        // 如果没有查询条件，使用 match_all
        if (boolQuery.must().isEmpty() && boolQuery.mustNot().isEmpty() && 
            boolQuery.should().isEmpty() && boolQuery.filter().isEmpty()) {
            return Query.of(q -> q.matchAll(m -> m));
        }
        return Query.of(q -> q.bool(boolQuery));
    }
    
    /**
     * 获取排序字段列表
     */
    public List<SortField> getSortFields() {
        return sortFields;
    }
    
    /**
     * 判断是否有排序条件
     */
    public boolean hasSort() {
        return !sortFields.isEmpty();
    }
}
