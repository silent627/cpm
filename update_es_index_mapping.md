# 更新 Elasticsearch 索引映射 - 日期格式修复

## 问题说明

ES 索引中的 `createTime` 和 `updateTime` 字段之前没有指定日期格式，默认只支持 ISO-8601 格式（如 `"2026-01-09T00:51:17"`），但后端发送的格式是 `"yyyy-MM-dd HH:mm:ss"`（如 `"2026-01-09 00:51:17"`），导致解析失败。

## 修复内容

已更新所有索引的映射配置，添加日期格式支持：
- `createTime`: 支持 `"yyyy-MM-dd HH:mm:ss"`、`"yyyy-MM-dd"`、ISO-8601 等多种格式
- `updateTime`: 支持 `"yyyy-MM-dd HH:mm:ss"`、`"yyyy-MM-dd"`、ISO-8601 等多种格式

## 更新步骤

### 方法1：通过 API 重建索引（推荐）

1. **重建户籍成员索引**（当前报错的索引）：
   ```bash
   # 使用 curl 或 Postman 调用重建索引 API
   POST http://localhost:8085/search/index/household-member/recreate
   ```

2. **重建其他索引**（如果需要）：
   ```bash
   POST http://localhost:8085/search/index/resident/recreate
   POST http://localhost:8085/search/index/household/recreate
   POST http://localhost:8085/search/index/user/recreate
   POST http://localhost:8085/search/index/admin/recreate
   ```

3. **重新同步数据**：
   ```bash
   POST http://localhost:8085/search/sync/all
   ```

### 方法2：手动删除并重建索引

1. **删除现有索引**（使用 ES API 或 Kibana）：
   ```bash
   # 删除户籍成员索引
   DELETE http://localhost:9200/household_member_index
   
   # 删除其他索引（如果需要）
   DELETE http://localhost:9200/resident_index
   DELETE http://localhost:9200/household_index
   DELETE http://localhost:9200/user_index
   DELETE http://localhost:9200/admin_index
   ```

2. **重启搜索服务**：
   - 重启 `cpm-search-service`
   - 服务启动时会自动创建索引（使用新的映射配置）

3. **重新同步数据**：
   - 等待自动同步，或手动触发同步

### 方法3：使用 PowerShell 脚本

```powershell
# 删除并重建户籍成员索引
$baseUrl = "http://localhost:8085"
Invoke-RestMethod -Uri "$baseUrl/search/index/household-member/recreate" -Method POST
Invoke-RestMethod -Uri "$baseUrl/search/sync/household-member" -Method POST

# 重建所有索引并同步数据
Invoke-RestMethod -Uri "$baseUrl/search/index/all/recreate" -Method POST
Invoke-RestMethod -Uri "$baseUrl/search/sync/all" -Method POST
```

## 验证步骤

### 1. 检查索引映射

```bash
# 检查户籍成员索引的映射
GET http://localhost:9200/household_member_index/_mapping

# 应该看到 createTime 和 updateTime 字段有 format 配置：
# "createTime": {
#   "type": "date",
#   "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis"
# }
```

### 2. 测试索引数据

```bash
# 尝试索引一个测试文档
POST http://localhost:9200/household_member_index/_doc/999
{
  "id": 999,
  "householdId": 1,
  "residentId": 1,
  "relationship": "户主",
  "createTime": "2026-01-09 00:51:17",
  "updateTime": "2026-01-09 00:51:17"
}

# 应该成功，没有 mapper_parsing_exception 错误
```

### 3. 检查同步日志

查看搜索服务日志，应该看到：
```
户籍成员数据同步完成，成功: 598 条，失败: 0 条，总计: 598 条
```

而不是：
```
户籍成员数据同步完成，成功: 0 条，失败: 598 条，总计: 598 条
```

## 注意事项

⚠️ **重要提示**：
- 删除索引会**丢失所有数据**，请确保已备份或可以重新同步
- 重建索引后，需要重新同步所有数据
- 建议在业务低峰期执行此操作

## 已更新的索引

以下索引的日期字段映射已更新：
1. ✅ `resident_index` - 居民索引
2. ✅ `household_index` - 户籍索引
3. ✅ `user_index` - 用户索引
4. ✅ `admin_index` - 管理员索引
5. ✅ `household_member_index` - 户籍成员索引

## 日期格式支持

更新后，所有日期字段支持以下格式：
- `yyyy-MM-dd HH:mm:ss` - 完整日期时间（主要格式）
- `yyyy-MM-dd` - 仅日期
- ISO-8601 格式（如 `2026-01-09T00:51:17`）
- 时间戳（毫秒）
