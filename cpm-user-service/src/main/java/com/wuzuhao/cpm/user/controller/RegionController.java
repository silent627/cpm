package com.wuzuhao.cpm.user.controller;

import com.wuzuhao.cpm.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * 行政区划控制器
 * 代理regions_data的API服务
 */
@Api(tags = "行政区划管理")
@RestController
@RequestMapping("/region")
public class RegionController {

    @Value("${regions.api.base-url:http://127.0.0.1:8000}")
    private String regionsApiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 获取所有省级行政区划
     */
    @ApiOperation(value = "获取所有省份", notes = "获取所有省级行政区划（省、直辖市、自治区、特别行政区）")
    @GetMapping("/provinces")
    public Result<List<Map<String, Object>>> getProvinces() {
        try {
            String url = regionsApiBaseUrl + "/api/provinces";
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            return Result.success(response.getBody());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 连接失败（服务未启动或网络问题）
            return Result.error("区划数据服务连接失败，请检查服务是否启动");
        } catch (Exception e) {
            return Result.error("获取省份列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定区划的下级行政区划
     */
    @ApiOperation(value = "获取下级行政区划", notes = "根据父级区划代码获取下级行政区划（自动适配直辖市）")
    @GetMapping("/children/{parentCode}")
    public Result<Map<String, Object>> getChildren(
            @ApiParam(value = "父级区划代码", required = true) @PathVariable String parentCode) {
        try {
            String url = regionsApiBaseUrl + "/api/children/" + parentCode;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return Result.success(response.getBody());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 连接失败（服务未启动或网络问题）
            return Result.error("区划数据服务连接失败，请检查服务是否启动");
        } catch (Exception e) {
            return Result.error("获取下级区划失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据统计信息
     */
    @ApiOperation(value = "获取数据统计", notes = "获取行政区划数据统计信息")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        try {
            String url = regionsApiBaseUrl + "/api/stats";
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return Result.success(response.getBody());
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }
}

