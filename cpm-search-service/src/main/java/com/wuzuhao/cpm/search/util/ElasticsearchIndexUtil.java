package com.wuzuhao.cpm.search.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringReader;

/**
 * Elasticsearch索引工具类
 * 用于创建和管理索引映射配置
 */
@Slf4j
@Component
public class ElasticsearchIndexUtil {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public static final String RESIDENT_INDEX = "resident_index";
    public static final String HOUSEHOLD_INDEX = "household_index";
    public static final String USER_INDEX = "user_index";
    public static final String ADMIN_INDEX = "admin_index";
    public static final String HOUSEHOLD_MEMBER_INDEX = "household_member_index";

    /**
     * 创建居民索引
     */
    public void createResidentIndex() {
        try {
            ElasticsearchIndicesClient indicesClient = elasticsearchClient.indices();
            
            // 检查索引是否存在
            BooleanResponse exists = indicesClient.exists(
                ExistsRequest.of(e -> e.index(RESIDENT_INDEX))
            );
            
            if (exists.value()) {
                log.info("居民索引 {} 已存在", RESIDENT_INDEX);
                return;
            }

            // 定义索引映射
            String mappingJson = "{\n" +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"id\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"userId\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"realName\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"standard\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"idCard\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"gender\": {\n" +
                "        \"type\": \"integer\"\n" +
                "      },\n" +
                "      \"birthDate\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis\"\n" +
                "      },\n" +
                "      \"nationality\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"registeredAddress\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"standard\"\n" +
                "      },\n" +
                "      \"currentAddress\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"standard\"\n" +
                "      },\n" +
                "      \"occupation\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"education\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"maritalStatus\": {\n" +
                "        \"type\": \"integer\"\n" +
                "      },\n" +
                "      \"contactPhone\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"emergencyContact\": {\n" +
                "        \"type\": \"text\"\n" +
                "      },\n" +
                "      \"emergencyPhone\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"remark\": {\n" +
                "        \"type\": \"text\"\n" +
                "      },\n" +
                "      \"createTime\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis\"\n" +
                "      },\n" +
                "      \"updateTime\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

            // 创建索引
            indicesClient.create(CreateIndexRequest.of(c -> c
                .index(RESIDENT_INDEX)
                .withJson(new StringReader(mappingJson))
            ));

            log.info("成功创建居民索引: {}", RESIDENT_INDEX);
        } catch (Exception e) {
            log.error("创建居民索引失败", e);
            throw new RuntimeException("创建居民索引失败", e);
        }
    }

    /**
     * 创建户籍索引
     */
    public void createHouseholdIndex() {
        try {
            ElasticsearchIndicesClient indicesClient = elasticsearchClient.indices();
            
            // 检查索引是否存在
            BooleanResponse exists = indicesClient.exists(
                ExistsRequest.of(e -> e.index(HOUSEHOLD_INDEX))
            );
            
            if (exists.value()) {
                log.info("户籍索引 {} 已存在", HOUSEHOLD_INDEX);
                return;
            }

            // 定义索引映射
            String mappingJson = "{\n" +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"id\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"headId\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"headName\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"standard\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"headIdCard\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"householdNo\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"address\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"standard\"\n" +
                "      },\n" +
                "      \"householdType\": {\n" +
                "        \"type\": \"integer\"\n" +
                "      },\n" +
                "      \"memberCount\": {\n" +
                "        \"type\": \"integer\"\n" +
                "      },\n" +
                "      \"contactPhone\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"moveInDate\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd||yyyy-MM-dd HH:mm:ss||strict_date_optional_time||epoch_millis\"\n" +
                "      },\n" +
                "      \"moveOutDate\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd||yyyy-MM-dd HH:mm:ss||strict_date_optional_time||epoch_millis\"\n" +
                "      },\n" +
                "      \"moveInReason\": {\n" +
                "        \"type\": \"text\"\n" +
                "      },\n" +
                "      \"moveOutReason\": {\n" +
                "        \"type\": \"text\"\n" +
                "      },\n" +
                "      \"status\": {\n" +
                "        \"type\": \"integer\"\n" +
                "      },\n" +
                "      \"remark\": {\n" +
                "        \"type\": \"text\"\n" +
                "      },\n" +
                "      \"createTime\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis\"\n" +
                "      },\n" +
                "      \"updateTime\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

            // 创建索引
            indicesClient.create(CreateIndexRequest.of(c -> c
                .index(HOUSEHOLD_INDEX)
                .withJson(new StringReader(mappingJson))
            ));

            log.info("成功创建户籍索引: {}", HOUSEHOLD_INDEX);
        } catch (Exception e) {
            log.error("创建户籍索引失败", e);
            throw new RuntimeException("创建户籍索引失败", e);
        }
    }

    /**
     * 创建用户索引
     */
    public void createUserIndex() {
        try {
            ElasticsearchIndicesClient indicesClient = elasticsearchClient.indices();
            
            // 检查索引是否存在
            BooleanResponse exists = indicesClient.exists(
                ExistsRequest.of(e -> e.index(USER_INDEX))
            );
            
            if (exists.value()) {
                log.info("用户索引 {} 已存在", USER_INDEX);
                return;
            }

            // 定义索引映射
            String mappingJson = "{\n" +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"id\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"username\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"standard\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"realName\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"standard\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"phone\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"email\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"avatar\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"role\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"status\": {\n" +
                "        \"type\": \"integer\"\n" +
                "      },\n" +
                "      \"createTime\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis\"\n" +
                "      },\n" +
                "      \"updateTime\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

            // 创建索引
            indicesClient.create(CreateIndexRequest.of(c -> c
                .index(USER_INDEX)
                .withJson(new StringReader(mappingJson))
            ));

            log.info("成功创建用户索引: {}", USER_INDEX);
        } catch (Exception e) {
            log.error("创建用户索引失败", e);
            throw new RuntimeException("创建用户索引失败", e);
        }
    }

    /**
     * 创建管理员索引
     */
    public void createAdminIndex() {
        try {
            ElasticsearchIndicesClient indicesClient = elasticsearchClient.indices();
            
            // 检查索引是否存在
            BooleanResponse exists = indicesClient.exists(
                ExistsRequest.of(e -> e.index(ADMIN_INDEX))
            );
            
            if (exists.value()) {
                log.info("管理员索引 {} 已存在", ADMIN_INDEX);
                return;
            }

            // 定义索引映射
            String mappingJson = "{\n" +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"id\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"userId\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"adminNo\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"standard\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"department\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"standard\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"position\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"remark\": {\n" +
                "        \"type\": \"text\"\n" +
                "      },\n" +
                "      \"createTime\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis\"\n" +
                "      },\n" +
                "      \"updateTime\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

            // 创建索引
            indicesClient.create(CreateIndexRequest.of(c -> c
                .index(ADMIN_INDEX)
                .withJson(new StringReader(mappingJson))
            ));

            log.info("成功创建管理员索引: {}", ADMIN_INDEX);
        } catch (Exception e) {
            log.error("创建管理员索引失败", e);
            throw new RuntimeException("创建管理员索引失败", e);
        }
    }

    /**
     * 创建户籍成员索引
     */
    public void createHouseholdMemberIndex() {
        try {
            ElasticsearchIndicesClient indicesClient = elasticsearchClient.indices();
            
            // 检查索引是否存在
            BooleanResponse exists = indicesClient.exists(
                ExistsRequest.of(e -> e.index(HOUSEHOLD_MEMBER_INDEX))
            );
            
            if (exists.value()) {
                log.info("户籍成员索引 {} 已存在", HOUSEHOLD_MEMBER_INDEX);
                return;
            }

            // 定义索引映射
            String mappingJson = "{\n" +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"id\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"householdId\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"residentId\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"relationship\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"createTime\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis\"\n" +
                "      },\n" +
                "      \"updateTime\": {\n" +
                "        \"type\": \"date\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

            // 创建索引
            indicesClient.create(CreateIndexRequest.of(c -> c
                .index(HOUSEHOLD_MEMBER_INDEX)
                .withJson(new StringReader(mappingJson))
            ));

            log.info("成功创建户籍成员索引: {}", HOUSEHOLD_MEMBER_INDEX);
        } catch (Exception e) {
            log.error("创建户籍成员索引失败", e);
            throw new RuntimeException("创建户籍成员索引失败", e);
        }
    }

    /**
     * 初始化所有索引
     */
    public void initIndices() {
        createResidentIndex();
        createHouseholdIndex();
        createUserIndex();
        createAdminIndex();
        createHouseholdMemberIndex();
    }

    /**
     * 删除并重建索引（用于索引重建功能）
     */
    public void recreateResidentIndex() {
        try {
            ElasticsearchIndicesClient indicesClient = elasticsearchClient.indices();
            BooleanResponse exists = indicesClient.exists(
                ExistsRequest.of(e -> e.index(RESIDENT_INDEX))
            );
            if (exists.value()) {
                indicesClient.delete(d -> d.index(RESIDENT_INDEX));
                log.info("已删除居民索引: {}", RESIDENT_INDEX);
            }
            createResidentIndex();
        } catch (Exception e) {
            log.error("重建居民索引失败", e);
            throw new RuntimeException("重建居民索引失败", e);
        }
    }

    /**
     * 删除并重建索引（用于索引重建功能）
     */
    public void recreateHouseholdIndex() {
        try {
            ElasticsearchIndicesClient indicesClient = elasticsearchClient.indices();
            BooleanResponse exists = indicesClient.exists(
                ExistsRequest.of(e -> e.index(HOUSEHOLD_INDEX))
            );
            if (exists.value()) {
                indicesClient.delete(d -> d.index(HOUSEHOLD_INDEX));
                log.info("已删除户籍索引: {}", HOUSEHOLD_INDEX);
            }
            createHouseholdIndex();
        } catch (Exception e) {
            log.error("重建户籍索引失败", e);
            throw new RuntimeException("重建户籍索引失败", e);
        }
    }

    /**
     * 删除并重建用户索引（用于索引重建功能）
     */
    public void recreateUserIndex() {
        try {
            ElasticsearchIndicesClient indicesClient = elasticsearchClient.indices();
            BooleanResponse exists = indicesClient.exists(
                ExistsRequest.of(e -> e.index(USER_INDEX))
            );
            if (exists.value()) {
                indicesClient.delete(d -> d.index(USER_INDEX));
                log.info("已删除用户索引: {}", USER_INDEX);
            }
            createUserIndex();
        } catch (Exception e) {
            log.error("重建用户索引失败", e);
            throw new RuntimeException("重建用户索引失败", e);
        }
    }

    /**
     * 删除并重建管理员索引（用于索引重建功能）
     */
    public void recreateAdminIndex() {
        try {
            ElasticsearchIndicesClient indicesClient = elasticsearchClient.indices();
            BooleanResponse exists = indicesClient.exists(
                ExistsRequest.of(e -> e.index(ADMIN_INDEX))
            );
            if (exists.value()) {
                indicesClient.delete(d -> d.index(ADMIN_INDEX));
                log.info("已删除管理员索引: {}", ADMIN_INDEX);
            }
            createAdminIndex();
        } catch (Exception e) {
            log.error("重建管理员索引失败", e);
            throw new RuntimeException("重建管理员索引失败", e);
        }
    }

    /**
     * 删除并重建户籍成员索引（用于索引重建功能）
     */
    public void recreateHouseholdMemberIndex() {
        try {
            ElasticsearchIndicesClient indicesClient = elasticsearchClient.indices();
            BooleanResponse exists = indicesClient.exists(
                ExistsRequest.of(e -> e.index(HOUSEHOLD_MEMBER_INDEX))
            );
            if (exists.value()) {
                indicesClient.delete(d -> d.index(HOUSEHOLD_MEMBER_INDEX));
                log.info("已删除户籍成员索引: {}", HOUSEHOLD_MEMBER_INDEX);
            }
            createHouseholdMemberIndex();
        } catch (Exception e) {
            log.error("重建户籍成员索引失败", e);
            throw new RuntimeException("重建户籍成员索引失败", e);
        }
    }
}
