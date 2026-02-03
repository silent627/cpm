package com.wuzuhao.cpm.search.service;

import com.wuzuhao.cpm.search.wrapper.ESQueryWrapper;

import java.util.List;
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
     * 搜索用户信息
     */
    Map<String, Object> searchUser(String keyword, Integer page, Integer size);
    
    /**
     * 搜索管理员信息
     */
    Map<String, Object> searchAdmin(String keyword, Integer page, Integer size);
    
    /**
     * 搜索户籍成员信息
     */
    Map<String, Object> searchHouseholdMember(String keyword, Integer page, Integer size, Long householdId);
    
    /**
     * 重建搜索索引
     */
    void rebuildIndex();
    
    // ========== 查询操作 ==========
    
    /**
     * 按ID查询文档
     * @param index 索引名称
     * @param id 文档ID
     * @return 文档数据
     */
    Map<String, Object> getById(String index, Long id);
    
    // ========== 条件查询 ==========
    
    /**
     * 条件查询列表
     * @param index 索引名称
     * @param wrapper 查询条件包装器
     * @return 文档列表
     */
    List<Map<String, Object>> list(String index, ESQueryWrapper wrapper);
    
    /**
     * 条件分页查询
     * @param index 索引名称
     * @param wrapper 查询条件包装器
     * @param current 当前页码（从1开始）
     * @param size 每页数量
     * @return 分页结果（包含 total, hits）
     */
    Map<String, Object> page(String index, ESQueryWrapper wrapper, Integer current, Integer size);
}

