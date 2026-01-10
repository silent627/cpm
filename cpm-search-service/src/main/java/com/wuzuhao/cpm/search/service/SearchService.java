package com.wuzuhao.cpm.search.service;

import java.util.Map;

/**
 * 搜索服务接口
 */
public interface SearchService {
    
    /**
     * 搜索居民信息
     */
    Map<String, Object> searchResident(String keyword, Integer page, Integer size);
    
    /**
     * 搜索户籍信息
     */
    Map<String, Object> searchHousehold(String keyword, Integer page, Integer size);
    
    /**
     * 重建搜索索引
     */
    void rebuildIndex();
}

