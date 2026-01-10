package com.wuzuhao.cpm.statistics.controller;

import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.statistics.service.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统计控制器
 */
@Api(tags = "数据统计")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取居民年龄分布统计
     */
    @ApiOperation(value = "获取居民年龄分布统计", notes = "按年龄段统计居民数量")
    @GetMapping("/resident/age-distribution")
    public Result<Map<String, Object>> getResidentAgeDistribution() {
        Map<String, Object> data = statisticsService.getResidentAgeDistribution();
        return Result.success(data);
    }

    /**
     * 获取居民性别统计
     */
    @ApiOperation(value = "获取居民性别统计", notes = "统计男女居民数量")
    @GetMapping("/resident/gender")
    public Result<Map<String, Object>> getResidentGenderStatistics() {
        Map<String, Object> data = statisticsService.getResidentGenderStatistics();
        return Result.success(data);
    }

    /**
     * 获取户籍类型统计
     */
    @ApiOperation(value = "获取户籍类型统计", notes = "统计家庭户和集体户数量")
    @GetMapping("/household/type")
    public Result<Map<String, Object>> getHouseholdTypeStatistics() {
        Map<String, Object> data = statisticsService.getHouseholdTypeStatistics();
        return Result.success(data);
    }

    /**
     * 获取户籍迁入迁出趋势
     */
    @ApiOperation(value = "获取户籍迁入迁出趋势", notes = "按月或年统计户籍迁入迁出趋势")
    @GetMapping("/household/move-trend")
    public Result<Map<String, Object>> getHouseholdMoveTrend(
            @ApiParam(value = "统计类型：month-月度，year-年度", example = "month") 
            @RequestParam(defaultValue = "month") String type) {
        Map<String, Object> data = statisticsService.getHouseholdMoveTrend(type);
        return Result.success(data);
    }

    /**
     * 获取月度数据统计
     */
    @ApiOperation(value = "获取月度数据统计", notes = "按月统计居民和户籍新增数量")
    @GetMapping("/monthly")
    public Result<Map<String, Object>> getMonthlyStatistics() {
        Map<String, Object> data = statisticsService.getMonthlyStatistics();
        return Result.success(data);
    }

    /**
     * 获取年度数据统计
     */
    @ApiOperation(value = "获取年度数据统计", notes = "按年统计居民和户籍新增数量")
    @GetMapping("/yearly")
    public Result<Map<String, Object>> getYearlyStatistics() {
        Map<String, Object> data = statisticsService.getYearlyStatistics();
        return Result.success(data);
    }
}

