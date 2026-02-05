# 日期格式验证步骤

## 修复内容

已修复以下三个方法中的日期解析问题：
1. `UserController.convertMapToUser` - 用户数据转换
2. `ResidentController.convertMapToResident` - 居民数据转换
3. `HouseholdController.convertMapToHousehold` - 户籍数据转换

## 修复说明

### 问题原因
- 原代码使用 `LocalDateTime.parse()` 默认格式（ISO-8601: `"2024-01-01T10:00:00"`）
- 后端返回的格式是 `"yyyy-MM-dd HH:mm:ss"`（如：`"2024-01-01 10:00:00"`）
- 格式不匹配导致解析失败，日期字段被设置为 `null`

### 修复方案
- 使用 `DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")` 指定日期格式
- 添加日志记录，便于排查问题

## 验证步骤

### 1. 检查后端日志

重启服务后，查看控制台日志，应该能看到：

**成功情况（正常）：**
```
成功解析用户 createTime: 2024-01-01 10:00:00
成功解析用户 updateTime: 2026-02-02 13:35:15
```

**失败情况（需要排查）：**
```
解析用户 createTime 失败: 2024-01-01, 错误: Text '2024-01-01' could not be parsed at index 10
```

### 2. 检查前端数据

打开浏览器开发者工具（F12），查看：

**Network 标签：**
- 找到用户/居民/户籍列表的 API 请求
- 查看响应数据中的 `createTime` 和 `updateTime` 字段
- 确认格式是否为 `"yyyy-MM-dd HH:mm:ss"`

**Console 标签：**
- 检查是否有 JavaScript 错误
- 检查是否有日期相关的错误信息

### 3. 测试数据展示

1. 打开用户列表页面
2. 检查"创建时间"列是否正常显示
3. 点击查看详情，检查"创建时间"和"更新时间"是否正常显示

### 4. 验证日期格式一致性

确认以下格式一致：
- ES 返回的日期格式：`"yyyy-MM-dd HH:mm:ss"`
- 后端解析的日期格式：`"yyyy-MM-dd HH:mm:ss"`
- 前端显示的日期格式：`"yyyy-MM-dd HH:mm:ss"`

## 常见问题排查

### 问题1：前端显示 "nodata"
**可能原因：**
- 日期解析失败，导致数据转换异常
- ES 返回的数据格式不正确

**解决方法：**
1. 查看后端日志，确认是否有日期解析失败的警告
2. 检查 ES 中的数据格式
3. 确认 `convertDateFields` 方法是否正确转换了日期格式

### 问题2：日期显示为 null 或空
**可能原因：**
- 日期字段解析失败
- ES 中该字段不存在

**解决方法：**
1. 查看后端日志中的警告信息
2. 检查 ES 中该文档的原始数据
3. 确认日期字段名称是否正确

### 问题3：日期格式不一致
**可能原因：**
- ES 中存在旧格式的数据（只有日期，没有时间）
- 新数据格式正确，但旧数据格式不正确

**解决方法：**
1. 重新同步数据到 ES
2. 或等待数据更新时自动同步新格式

## 测试命令

### 检查 ES 中的日期格式
```bash
# 检查用户索引中的日期格式
curl -X GET "http://localhost:9200/user_index/_doc/1" | jq '._source.createTime'

# 检查居民索引中的日期格式
curl -X GET "http://localhost:9200/resident_index/_doc/1" | jq '._source.createTime'

# 检查户籍索引中的日期格式
curl -X GET "http://localhost:9200/household_index/_doc/1" | jq '._source.createTime'
```

### 检查后端日志
```bash
# 查看用户服务日志（Windows PowerShell）
Get-Content -Path "logs\cpm-user-service.log" -Tail 50 | Select-String "解析.*Time"

# 查看居民服务日志
Get-Content -Path "logs\cpm-resident-service.log" -Tail 50 | Select-String "解析.*Time"

# 查看户籍服务日志
Get-Content -Path "logs\cpm-household-service.log" -Tail 50 | Select-String "解析.*Time"
```

## 预期结果

修复后，应该：
1. ✅ 后端日志显示日期解析成功
2. ✅ 前端页面正常显示数据
3. ✅ 日期字段显示为完整格式：`"2024-01-01 10:00:00"`
4. ✅ 没有日期解析失败的警告日志
