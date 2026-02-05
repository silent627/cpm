package com.wuzuhao.cpm.search.service;

import java.util.List;
import java.util.Map;

/**
 * ES文档同步服务接口（内部使用）
 * 仅用于ESDataSyncListener进行数据同步
 * 不对外暴露，确保ES只用于查询，不用于业务数据的增删改
 */
public interface ESDocumentSyncService {
    /**
     * 创建文档
     * @param index 索引名称
     * @param document 文档数据
     * @return 创建的文档ID
     */
    String saveDocument(String index, Map<String, Object> document);
    
    /**
     * 更新文档
     * @param index 索引名称
     * @param id 文档ID
     * @param document 更新的文档数据
     * @return 是否成功
     */
    boolean updateDocument(String index, Long id, Map<String, Object> document);
    
    /**
     * 删除文档
     * @param index 索引名称
     * @param id 文档ID
     * @return 是否成功
     */
    boolean removeDocument(String index, Long id);
    
    /**
     * 批量创建文档
     * @param index 索引名称
     * @param documents 文档列表
     * @return 成功数量
     */
    int saveBatchDocuments(String index, List<Map<String, Object>> documents);
    
    /**
     * 批量更新文档
     * @param index 索引名称
     * @param documents 文档列表（必须包含id字段）
     * @return 成功数量
     */
    int updateBatchDocuments(String index, List<Map<String, Object>> documents);
    
    /**
     * 批量删除文档
     * @param index 索引名称
     * @param ids ID列表
     * @return 成功数量
     */
    int removeBatchDocuments(String index, List<Long> ids);
}
