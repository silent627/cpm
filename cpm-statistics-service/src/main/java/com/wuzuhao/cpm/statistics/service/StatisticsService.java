package com.wuzuhao.cpm.statistics.service;

import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {
    
    /**
     * 获取居民年龄分布统计
     */
    Map<String, Object> getResidentAgeDistribution();
    
    /**
     * 获取居民性别统计
     */
    Map<String, Object> getResidentGenderStatistics();
    
    /**
     * 获取户籍类型统计
     */
    Map<String, Object> getHouseholdTypeStatistics();
    
    /**
     * 获取户籍迁入迁出趋势（月度）
     */
    Map<String, Object> getHouseholdMoveTrend(String type); // type: "month" or "year"
    
    /**
     * 获取月度数据统计
     */
    Map<String, Object> getMonthlyStatistics();
    
    /**
     * 获取年度数据统计
     */
    Map<String, Object> getYearlyStatistics();
}

