# JMeter登录接口测试方案

## 1. 测试计划结构

```
登录接口测试计划
├── 测试计划
│   ├── HTTP信息头管理器
│   ├── CSV数据集配置
│   ├── 线程组
│   │   ├── HTTP请求（登录）
│   │   ├── JSON断言
│   │   └── 正则表达式提取器（提取Token）
│   └── 监听器（聚合报告）
```

## 2. 配置步骤

### 2.1 测试计划配置

- **名称**：登录接口性能测试
- **注释**：测试登录接口的并发性能

### 2.2 HTTP信息头管理器

**位置**：测试计划 > 添加 > 配置元件 > HTTP信息头管理器

| 名称 | 值 |
|------|-----|
| Content-Type | application/json |

### 2.3 CSV数据集配置

**位置**：测试计划 > 添加 > 配置元件 > CSV数据集配置

| 参数 | 值 | 说明 |
|------|-----|------|
| 文件名 | `users.csv` | 测试账号文件路径 |
| 变量名称 | `username,password` | 逗号分隔 |
| 忽略首行 | `false` | 如果CSV有表头则设为true |
| 分隔符 | `,` | CSV分隔符 |
| 是否允许带引号 | `true` | 允许CSV字段带引号 |
| 遇到文件结束符再次循环 | `true` | 循环使用数据 |
| 遇到文件结束符停止线程 | `false` | 不停止线程 |

**users.csv 文件内容示例：**
```csv
username,password
admin,admin123
user1,123456
user2,123456
user3,123456
```

### 2.4 线程组配置

**位置**：测试计划 > 添加 > 线程（用户）> 线程组

| 参数 | 值 | 说明 |
|------|-----|------|
| 线程数 | `100` | 并发用户数 |
| Ramp-up时间 | `10` | 10秒内启动100个线程 |
| 循环次数 | `10` | 每个线程执行10次 |
| 调度器 | 勾选 | 启用调度器 |
| 持续时间 | `300` | 运行300秒（5分钟） |

### 2.5 HTTP请求配置

**位置**：线程组 > 添加 > 取样器 > HTTP请求

| 参数 | 值 |
|------|-----|
| 名称 | 用户登录 |
| 协议 | `http` |
| 服务器名称或IP | `localhost` |
| 端口号 | `8080` |
| 方法 | `POST` |
| 路径 | `/api/auth/login` |
| 内容编码 | `UTF-8` |

**Body Data（消息体数据）：**
```json
{
  "username": "${username}",
  "password": "${password}"
}
```

### 2.6 JSON断言

**位置**：HTTP请求 > 添加 > 断言 > JSON断言

| 参数 | 值 |
|------|-----|
| Assert JSON Path exists | `$.code` |
| Additionally assert value | 勾选 |
| Expected Value | `200` |
| Match | `Equals` |

### 2.7 正则表达式提取器（可选）

**位置**：HTTP请求 > 添加 > 后置处理器 > 正则表达式提取器

**用途**：提取Token供后续接口使用

| 参数 | 值 |
|------|-----|
| 引用名称 | `token` |
| 正则表达式 | `"token":"(.+?)"` |
| 模板 | `$1$` |
| 匹配数字 | `1` |
| 缺省值 | `NOT_FOUND` |

### 2.8 监听器配置

#### 2.8.1 聚合报告（必需）

**位置**：线程组 > 添加 > 监听器 > 聚合报告

**作用**：统计响应时间、吞吐量、错误率等关键指标

#### 2.8.2 查看结果树（调试用，正式测试可禁用）

**位置**：线程组 > 添加 > 监听器 > 查看结果树

**说明**：仅用于调试，正式测试时建议禁用（会消耗大量内存）

## 3. 性能指标配置

### 3.1 响应时间要求

- P50（中位数）：≤ 200ms
- P95：≤ 500ms
- P99：≤ 1000ms

### 3.2 吞吐量要求

- TPS：≥ 200

### 3.3 成功率要求

- 成功率：≥ 99.95%

## 4. 测试场景

### 场景1：正常负载测试
- 线程数：100
- Ramp-up：10秒
- 持续时间：5分钟

### 场景2：峰值负载测试
- 线程数：300
- Ramp-up：30秒
- 持续时间：10分钟

### 场景3：压力测试
- 线程数：500
- Ramp-up：60秒
- 持续时间：15分钟

## 5. 执行步骤

1. **准备测试数据**：创建`users.csv`文件，包含至少500个测试账号
2. **配置测试计划**：按照上述步骤配置JMeter测试计划
3. **保存测试计划**：保存为`登录接口测试.jmx`
4. **执行测试**：
   ```bash
   # 命令行执行（无GUI模式，性能更好）
   jmeter -n -t 登录接口测试.jmx -l 测试结果.jtl -e -o 测试报告/
   ```
5. **查看报告**：打开生成的HTML报告查看详细结果

## 6. 结果分析要点

- **响应时间**：关注P95和P99响应时间
- **吞吐量**：查看TPS是否达到要求
- **错误率**：检查错误率和错误类型
- **资源使用**：结合系统监控查看CPU、内存使用情况

## 7. 取消限流配置

### 7.1 应用配置方式（推荐）

在测试环境的 `application.yml` 中禁用限流：

**位置**：各微服务的 `src/main/resources/application.yml`

```yaml
cpm:
  rate-limit:
    enabled: false  # 禁用限流，测试时设置为false
    max-requests-per-minute: 180  # 如果启用限流，可调整此值
```

**配置说明**：
- `enabled: false` - 完全禁用限流拦截器
- `enabled: true` - 启用限流，使用 `max-requests-per-minute` 配置的值

### 7.2 配置范围

需要在以下微服务的配置文件中添加：

| 微服务 | 配置文件路径 |
|--------|------------|
| cpm-user-service | `cpm-user-service/src/main/resources/application.yml` |
| cpm-resident-service | `cpm-resident-service/src/main/resources/application.yml` |
| cpm-household-service | `cpm-household-service/src/main/resources/application.yml` |
| cpm-search-service | `cpm-search-service/src/main/resources/application.yml` |
| cpm-file-service | `cpm-file-service/src/main/resources/application.yml` |
| cpm-statistics-service | `cpm-statistics-service/src/main/resources/application.yml` |
| cpm-notification-service | `cpm-notification-service/src/main/resources/application.yml` |

### 7.3 配置生效

1. **修改配置后重启服务**：修改配置后需要重启对应的微服务
2. **验证配置**：可以通过日志确认限流是否已禁用
3. **测试后恢复**：测试完成后建议恢复限流配置，避免影响生产环境

### 7.4 临时禁用方式（不推荐）

如果无法修改配置文件，可以临时修改代码：

**文件**：`cpm-common/src/main/java/com/wuzuhao/cpm/config/RateLimitProperties.java`

```java
private boolean enabled = false;  // 临时改为false
```

**注意**：此方式需要重新编译和部署，不推荐用于测试环境

## 8. 注意事项

1. **测试环境**：确保测试环境与生产环境配置一致
2. **数据准备**：准备足够的测试账号，避免账号锁定
3. **限流处理**：
   - 性能测试时建议禁用限流（设置 `cpm.rate-limit.enabled=false`）
   - 测试完成后恢复限流配置
4. **Token管理**：如需后续接口测试，保存Token到变量
5. **监控系统**：同时监控应用服务器、数据库、Redis等资源

## 9. 快速配置模板

**最小化配置（仅必需组件）：**
1. 线程组
2. HTTP信息头管理器
3. CSV数据集配置
4. HTTP请求
5. JSON断言
6. 聚合报告

**禁用组件（节省资源）：**
- 查看结果树（正式测试时）
- 图形结果
- 其他非必需监听器
