package com.wuzuhao.cpm.search.controller;

import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.search.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 搜索控制器
 */
@Api(tags = "搜索服务")
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @ApiOperation("搜索居民信息")
    @GetMapping("/resident")
    public Result<Map<String, Object>> searchResident(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = searchService.searchResident(keyword, page, size);
        return Result.success(result);
    }

    @ApiOperation("搜索户籍信息")
    @GetMapping("/household")
    public Result<Map<String, Object>> searchHousehold(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = searchService.searchHousehold(keyword, page, size);
        return Result.success(result);
    }

    @ApiOperation("重建搜索索引")
    @PostMapping("/index/rebuild")
    public Result<Void> rebuildIndex() {
        searchService.rebuildIndex();
        return Result.success();
    }
}

