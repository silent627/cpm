package com.wuzuhao.cpm.search.controller;

import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.search.service.SearchService;
import com.wuzuhao.cpm.search.wrapper.ESQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @ApiOperation("搜索用户信息")
    @GetMapping("/user")
    public Result<Map<String, Object>> searchUser(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = searchService.searchUser(keyword, page, size);
        return Result.success(result);
    }

    @ApiOperation("搜索管理员信息")
    @GetMapping("/admin")
    public Result<Map<String, Object>> searchAdmin(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = searchService.searchAdmin(keyword, page, size);
        return Result.success(result);
    }

    @ApiOperation("搜索户籍成员信息")
    @GetMapping("/household-member")
    public Result<Map<String, Object>> searchHouseholdMember(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long householdId) {
        Map<String, Object> result = searchService.searchHouseholdMember(keyword, page, size, householdId);
        return Result.success(result);
    }

    @ApiOperation("重建搜索索引")
    @PostMapping("/index/rebuild")
    public Result<Void> rebuildIndex() {
        searchService.rebuildIndex();
        return Result.success();
    }
    
    // ========== 查询操作接口 ==========
    
    @ApiOperation("按ID查询文档")
    @GetMapping("/{index}/{id}")
    public Result<Map<String, Object>> getById(
            @PathVariable String index,
            @PathVariable Long id) {
        Map<String, Object> result = searchService.getById(index, id);
        if (result != null) {
            return Result.success(result);
        }
        return Result.error("文档不存在");
    }
    
    // ========== 条件查询接口 ==========
    
    @ApiOperation("条件查询列表")
    @PostMapping("/{index}/list")
    public Result<List<Map<String, Object>>> list(
            @PathVariable String index,
            @RequestBody ESQueryWrapper wrapper) {
        List<Map<String, Object>> result = searchService.list(index, wrapper);
        return Result.success(result);
    }
    
    @ApiOperation("条件分页查询")
    @PostMapping("/{index}/page")
    public Result<Map<String, Object>> page(
            @PathVariable String index,
            @RequestBody ESQueryWrapper wrapper,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = searchService.page(index, wrapper, current, size);
        return Result.success(result);
    }
}

