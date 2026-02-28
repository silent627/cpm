# 社区人口管理系统 Postman 接口测试需求文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 文档名称 | 社区人口管理系统 Postman 接口测试需求文档 |
| 文档版本 | v1.0.0 |
| 创建日期 | 2025-01-XX |
| 维护者 | 测试团队 |
| 项目名称 | 社区人口管理系统（CPM） |
| 参考文档 | 测试需求文档.md、API_DOCUMENTATION.md |

---

## 1. 概述

### 1.1 文档目的

本文档详细说明了使用 Postman 对社区人口管理系统进行接口测试的需求，包括测试集合的组织结构、环境变量配置、测试用例设计、测试脚本编写等。

### 1.2 测试范围

本次 Postman 接口测试涵盖以下模块：
- 认证管理模块
- 用户管理模块
- 居民管理模块
- 搜索服务模块
- 文件上传模块
- 统计服务模块

### 1.3 测试目标

- 验证接口功能正确性
- 验证接口响应时间是否符合要求
- 验证接口错误处理机制
- 验证接口权限控制
- 为性能测试提供基础测试脚本

---

## 2. Postman 集合组织结构

### 2.1 集合层级结构

```
CPM 社区人口管理系统
├── 01_认证管理
│   ├── 用户登录
│   ├── 用户登出
│   ├── 发送忘记密码验证码
│   ├── 验证忘记密码验证码
│   └── 重置密码（忘记密码）
├── 02_用户管理
│   ├── 获取当前用户信息
│   ├── 更新用户信息
│   ├── 分页查询用户列表
│   ├── 根据ID获取用户
│   └── 导出用户列表
├── 03_居民管理
│   ├── 分页查询居民列表
│   ├── 根据ID获取居民
│   ├── 根据身份证号查询居民
│   └── 导出居民列表
├── 04_搜索服务
│   ├── 搜索居民信息
│   ├── 搜索户籍信息
│   └── 搜索用户信息
├── 05_文件上传
│   ├── 上传头像
│   └── 通用文件上传
└── 06_统计服务
    ├── 获取居民年龄分布统计
    ├── 获取居民性别统计
    ├── 获取月度数据统计
    └── 获取年度数据统计
```

### 2.2 集合命名规范

- **集合名称**：`CPM 社区人口管理系统`
- **文件夹名称**：`01_模块名称`（使用数字前缀便于排序）
- **请求名称**：`接口功能描述`（简洁明了）

---

## 3. 环境变量配置

### 3.1 环境变量列表

#### 3.1.1 基础环境变量

| 变量名 | 初始值 | 说明 | 类型 |
|--------|--------|------|------|
| `base_url` | `http://localhost:8080` | 基础URL | string |
| `token` | (空) | JWT Token，登录后自动设置 | string |
| `user_id` | (空) | 当前用户ID，登录后自动设置 | string |
| `username` | `admin` | 测试用户名 | string |
| `password` | `admin123` | 测试用户密码 | string |
| `admin_token` | (空) | 管理员Token | string |
| `user_token` | (空) | 普通用户Token | string |

#### 3.1.2 测试数据变量

| 变量名 | 初始值 | 说明 | 类型 |
|--------|--------|------|------|
| `test_resident_id` | (空) | 测试用居民ID | number |
| `test_user_id` | (空) | 测试用用户ID | number |
| `test_household_id` | (空) | 测试用户籍ID | number |
| `test_id_card` | `110101199001011234` | 测试用身份证号 | string |
| `test_email` | `test@example.com` | 测试用邮箱 | string |

### 3.2 环境配置

#### 3.2.1 开发环境（Development）

```json
{
  "base_url": "http://localhost:8080",
  "username": "admin",
  "password": "admin123"
}
```

#### 3.2.2 测试环境（Testing）

```json
{
  "base_url": "http://test-server:8080",
  "username": "test_admin",
  "password": "test123456"
}
```

#### 3.2.3 生产环境（Production）

```json
{
  "base_url": "https://api.cpm.example.com",
  "username": "prod_admin",
  "password": "prod_password"
}
```

---

## 4. 测试用例详细设计

### 4.1 认证管理模块

#### 4.1.1 用户登录

**请求配置：**
- **Method**: `POST`
- **URL**: `{{base_url}}/api/auth/login`
- **Headers**: 
  ```
  Content-Type: application/json
  ```

**请求体：**
```json
{
  "username": "{{username}}",
  "password": "{{password}}"
}
```

**测试用例：**

| 用例编号 | 用例名称 | 请求参数 | 预期结果 | 验证点 |
|---------|---------|---------|---------|--------|
| TC-AUTH-001 | 正常登录 | username: admin, password: admin123 | 返回200，包含token | status=200, 存在token字段 |
| TC-AUTH-002 | 用户名错误 | username: wrong, password: admin123 | 返回500，提示用户名或密码错误 | status=500, message包含错误信息 |
| TC-AUTH-003 | 密码错误 | username: admin, password: wrong | 返回500，提示用户名或密码错误 | status=500, message包含错误信息 |
| TC-AUTH-004 | 参数为空 | username: "", password: "" | 返回400，参数验证失败 | status=400 |
| TC-AUTH-005 | 缺少参数 | 无username或password | 返回400，参数验证失败 | status=400 |

**Pre-request Script：**
```javascript
// 可以动态生成测试用户名
// pm.environment.set("username", "test_user_" + Date.now());
```

**Tests Script：**
```javascript
// 验证响应状态码
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// 验证响应时间
pm.test("Response time is less than 200ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(200);
});

// 验证响应格式
pm.test("Response has correct structure", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('code');
    pm.expect(jsonData).to.have.property('message');
    pm.expect(jsonData).to.have.property('data');
});

// 验证登录成功
pm.test("Login successful", function () {
    var jsonData = pm.response.json();
    if (jsonData.code === 200) {
        pm.expect(jsonData.data).to.have.property('token');
        pm.expect(jsonData.data).to.have.property('userId');
        
        // 保存token到环境变量
        pm.environment.set("token", jsonData.data.token);
        pm.environment.set("user_id", jsonData.data.userId);
        pm.environment.set("admin_token", jsonData.data.token);
    }
});

// 验证响应时间（P95要求）
pm.test("Response time P95 requirement", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});
```

---

#### 4.1.2 用户登出

**请求配置：**
- **Method**: `POST`
- **URL**: `{{base_url}}/api/auth/logout`
- **Headers**: 
  ```
  Authorization: Bearer {{token}}
  Content-Type: application/json
  ```

**Tests Script：**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Logout successful", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
    
    // 清除token（可选）
    // pm.environment.unset("token");
});
```

---

### 4.2 用户管理模块

#### 4.2.1 获取当前用户信息

**请求配置：**
- **Method**: `GET`
- **URL**: `{{base_url}}/api/user/info`
- **Headers**: 
  ```
  Authorization: Bearer {{token}}
  ```

**测试用例：**

| 用例编号 | 用例名称 | 请求参数 | 预期结果 | 验证点 |
|---------|---------|---------|---------|--------|
| TC-USER-001 | 正常获取 | 有效token | 返回200，包含用户信息 | status=200, 包含用户详细信息 |
| TC-USER-002 | Token无效 | 无效token | 返回401，未授权 | status=401 |
| TC-USER-003 | Token过期 | 过期token | 返回401，Token已过期 | status=401 |
| TC-USER-004 | 无Token | 无Authorization头 | 返回401，未授权 | status=401 |

**Tests Script：**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response time is less than 200ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(200);
});

pm.test("User info structure is correct", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
    pm.expect(jsonData.data).to.have.property('id');
    pm.expect(jsonData.data).to.have.property('username');
    pm.expect(jsonData.data).to.have.property('role');
});

pm.test("Response time P95 requirement", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});
```

---

#### 4.2.2 分页查询用户列表

**请求配置：**
- **Method**: `GET`
- **URL**: `{{base_url}}/api/user/list`
- **Headers**: 
  ```
  Authorization: Bearer {{admin_token}}
  ```
- **Params**:
  ```
  current: 1
  size: 10
  username: (可选)
  role: (可选)
  ```

**测试用例：**

| 用例编号 | 用例名称 | 请求参数 | 预期结果 | 验证点 |
|---------|---------|---------|---------|--------|
| TC-USER-005 | 正常分页查询 | current=1, size=10 | 返回200，分页数据 | status=200, 包含records和total |
| TC-USER-006 | 按用户名查询 | username=admin | 返回200，筛选结果 | status=200, 结果包含admin |
| TC-USER-007 | 按角色查询 | role=ADMIN | 返回200，筛选结果 | status=200, 所有结果role=ADMIN |
| TC-USER-008 | 无权限访问 | 普通用户token | 返回403，权限不足 | status=403 |
| TC-USER-009 | 超大页码 | current=999999 | 返回200，空结果 | status=200, records为空 |

**Tests Script：**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response time is less than 300ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(300);
});

pm.test("Pagination structure is correct", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
    pm.expect(jsonData.data).to.have.property('records');
    pm.expect(jsonData.data).to.have.property('total');
    pm.expect(jsonData.data).to.have.property('current');
    pm.expect(jsonData.data).to.have.property('size');
    pm.expect(jsonData.data).to.have.property('pages');
});

pm.test("Records is array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data.records).to.be.an('array');
});

pm.test("Response time P95 requirement", function () {
    pm.expect(pm.response.responseTime).to.be.below(800);
});
```

---

### 4.3 居民管理模块

#### 4.3.1 分页查询居民列表

**请求配置：**
- **Method**: `GET`
- **URL**: `{{base_url}}/api/resident/list`
- **Headers**: 
  ```
  Authorization: Bearer {{admin_token}}
  ```
- **Params**:
  ```
  current: 1
  size: 10
  realName: (可选)
  idCard: (可选)
  currentAddress: (可选)
  ```

**Tests Script：**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response time is less than 300ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(300);
});

pm.test("Resident list structure is correct", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
    pm.expect(jsonData.data).to.have.property('records');
    pm.expect(jsonData.data).to.have.property('total');
    
    // 如果有多条记录，保存第一条记录的ID用于后续测试
    if (jsonData.data.records && jsonData.data.records.length > 0) {
        pm.environment.set("test_resident_id", jsonData.data.records[0].id);
    }
});

pm.test("Response time P95 requirement", function () {
    pm.expect(pm.response.responseTime).to.be.below(800);
});
```

---

#### 4.3.2 根据ID获取居民

**请求配置：**
- **Method**: `GET`
- **URL**: `{{base_url}}/api/resident/{{test_resident_id}}`
- **Headers**: 
  ```
  Authorization: Bearer {{admin_token}}
  ```

**Tests Script：**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response time is less than 200ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(200);
});

pm.test("Resident detail structure is correct", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
    pm.expect(jsonData.data).to.have.property('id');
    pm.expect(jsonData.data).to.have.property('realName');
    pm.expect(jsonData.data).to.have.property('idCard');
});

pm.test("Response time P95 requirement", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});
```

---

### 4.4 搜索服务模块

#### 4.4.1 搜索居民信息

**请求配置：**
- **Method**: `GET`
- **URL**: `{{base_url}}/api/search/resident`
- **Headers**: 
  ```
  Authorization: Bearer {{token}}
  ```
- **Params**:
  ```
  keyword: 张三
  page: 0
  size: 10
  ```

**测试用例：**

| 用例编号 | 用例名称 | 请求参数 | 预期结果 | 验证点 |
|---------|---------|---------|---------|--------|
| TC-SEARCH-001 | 精确搜索 | keyword=张三 | 返回200，匹配结果 | status=200, 结果包含"张三" |
| TC-SEARCH-002 | 模糊搜索 | keyword=张 | 返回200，包含"张"的结果 | status=200, 结果包含"张" |
| TC-SEARCH-003 | 空关键词 | keyword= | 返回200，所有结果 | status=200 |
| TC-SEARCH-004 | 无结果搜索 | keyword=不存在的名字 | 返回200，空结果 | status=200, total=0 |
| TC-SEARCH-005 | 分页测试 | page=1, size=5 | 返回200，正确分页 | status=200, hits数量≤5 |

**Tests Script：**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response time is less than 500ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

pm.test("Search result structure is correct", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
    pm.expect(jsonData.data).to.have.property('hits');
    pm.expect(jsonData.data).to.have.property('total');
    pm.expect(jsonData.data).to.have.property('page');
    pm.expect(jsonData.data).to.have.property('size');
    pm.expect(jsonData.data.hits).to.be.an('array');
});

pm.test("Response time P95 requirement", function () {
    pm.expect(pm.response.responseTime).to.be.below(1500);
});
```

---

### 4.5 文件上传模块

#### 4.5.1 上传头像

**请求配置：**
- **Method**: `POST`
- **URL**: `{{base_url}}/api/upload/avatar`
- **Headers**: 
  ```
  Authorization: Bearer {{token}}
  ```
- **Body**: `form-data`
  - Key: `file`
  - Type: `File`
  - Value: 选择本地图片文件

**测试用例：**

| 用例编号 | 用例名称 | 请求参数 | 预期结果 | 验证点 |
|---------|---------|---------|---------|--------|
| TC-UPLOAD-001 | 正常上传 | 有效图片文件(<10MB) | 返回200，文件URL | status=200, 返回文件路径 |
| TC-UPLOAD-002 | 文件过大 | 文件>10MB | 返回400/500，错误提示 | status=400/500 |
| TC-UPLOAD-003 | 不支持格式 | 非图片格式文件 | 返回400，格式错误 | status=400 |
| TC-UPLOAD-004 | 无文件 | 未选择文件 | 返回400，参数错误 | status=400 |

**Tests Script：**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response time is less than 2000ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(2000);
});

pm.test("Upload successful", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
    pm.expect(jsonData.data).to.be.a('string');
    pm.expect(jsonData.data).to.include('/uploads/');
});

pm.test("Response time P95 requirement", function () {
    pm.expect(pm.response.responseTime).to.be.below(5000);
});
```

---

### 4.6 统计服务模块

#### 4.6.1 获取月度数据统计

**请求配置：**
- **Method**: `GET`
- **URL**: `{{base_url}}/api/statistics/monthly`
- **Headers**: 
  ```
  Authorization: Bearer {{admin_token}}
  ```

**Tests Script：**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response time is less than 1000ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(1000);
});

pm.test("Statistics structure is correct", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
    pm.expect(jsonData.data).to.have.property('categories');
    pm.expect(jsonData.data).to.have.property('resident');
    pm.expect(jsonData.data).to.have.property('household');
    pm.expect(jsonData.data.categories).to.be.an('array');
    pm.expect(jsonData.data.resident).to.be.an('array');
    pm.expect(jsonData.data.household).to.be.an('array');
});

pm.test("Response time P95 requirement", function () {
    pm.expect(pm.response.responseTime).to.be.below(3000);
});
```

---

## 5. 集合级别的脚本

### 5.1 Collection Pre-request Script

在集合级别添加 Pre-request Script，用于全局设置：

```javascript
// 设置请求超时时间
pm.request.headers.add({
    key: 'X-Request-Id',
    value: pm.variables.replaceIn('{{$randomUUID}}')
});

// 记录请求开始时间
pm.environment.set("request_start_time", Date.now());
```

### 5.2 Collection Tests Script

在集合级别添加 Tests Script，用于全局验证：

```javascript
// 验证响应时间（全局）
var responseTime = pm.response.responseTime;
pm.test("Response time is acceptable", function () {
    pm.expect(responseTime).to.be.below(10000); // 10秒超时
});

// 记录响应时间到环境变量（用于性能分析）
var currentTime = Date.now();
var requestStartTime = pm.environment.get("request_start_time");
if (requestStartTime) {
    var actualResponseTime = currentTime - parseInt(requestStartTime);
    console.log("Request: " + pm.info.requestName + ", Response Time: " + actualResponseTime + "ms");
}
```

---

## 6. 测试数据准备

### 6.1 测试账号准备

| 角色 | 用户名 | 密码 | 用途 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 管理员权限测试 |
| 普通用户 | testuser | 123456 | 普通用户权限测试 |
| 测试用户1 | test1 | 123456 | 并发测试 |
| 测试用户2 | test2 | 123456 | 并发测试 |

### 6.2 测试数据文件

创建以下测试数据文件：

1. **test_users.json** - 用户测试数据
2. **test_residents.json** - 居民测试数据
3. **test_files/** - 测试文件目录
   - small_image.jpg (< 1MB)
   - medium_image.jpg (1-5MB)
   - large_image.jpg (5-10MB)
   - invalid_file.txt (不支持格式)

### 6.3 数据初始化脚本

在 Postman 中创建数据初始化请求：

**请求名称**: `数据初始化 - 登录并获取Token`

**Pre-request Script：**
```javascript
// 使用不同的测试账号
var testUsers = [
    {username: "admin", password: "admin123"},
    {username: "testuser", password: "123456"}
];
var randomUser = testUsers[Math.floor(Math.random() * testUsers.length)];
pm.environment.set("username", randomUser.username);
pm.environment.set("password", randomUser.password);
```

**Tests Script：**
```javascript
// 保存token
var jsonData = pm.response.json();
if (jsonData.code === 200) {
    pm.environment.set("token", jsonData.data.token);
    pm.environment.set("user_id", jsonData.data.userId);
}
```

---

## 7. 测试执行顺序

### 7.1 基础流程测试

1. **用户登录** → 获取Token
2. **获取当前用户信息** → 验证Token有效性
3. **分页查询用户列表** → 验证管理员权限
4. **分页查询居民列表** → 验证数据查询
5. **搜索居民信息** → 验证搜索功能
6. **获取月度数据统计** → 验证统计功能
7. **用户登出** → 清理会话

### 7.2 完整测试流程

使用 Postman Collection Runner 执行以下顺序：

```
1. 认证管理
   ├── 用户登录（管理员）
   ├── 用户登录（普通用户）
   └── 用户登出

2. 用户管理
   ├── 获取当前用户信息
   ├── 更新用户信息
   ├── 分页查询用户列表
   └── 根据ID获取用户

3. 居民管理
   ├── 分页查询居民列表
   ├── 根据ID获取居民
   └── 根据身份证号查询居民

4. 搜索服务
   ├── 搜索居民信息
   ├── 搜索户籍信息
   └── 搜索用户信息

5. 文件上传
   ├── 上传头像
   └── 通用文件上传

6. 统计服务
   ├── 获取居民年龄分布统计
   ├── 获取月度数据统计
   └── 获取年度数据统计
```

---

## 8. 测试报告要求

### 8.1 测试结果验证

每个测试用例应验证以下内容：

1. **状态码验证**：响应状态码是否符合预期
2. **响应时间验证**：响应时间是否满足性能要求
3. **数据结构验证**：响应数据格式是否正确
4. **业务逻辑验证**：返回数据是否符合业务逻辑
5. **错误处理验证**：错误场景是否正确处理

### 8.2 性能指标记录

记录以下性能指标：

- **响应时间**：P50、P95、P99
- **成功率**：接口调用成功率
- **错误率**：各类错误的发生率
- **吞吐量**：QPS/TPS（通过Collection Runner统计）

### 8.3 测试报告模板

```markdown
## 测试报告

### 测试环境
- 环境名称：开发环境
- 测试时间：2025-01-XX
- 测试人员：XXX

### 测试结果汇总
- 总用例数：XX
- 通过数：XX
- 失败数：XX
- 通过率：XX%

### 性能指标
| 接口名称 | P50响应时间 | P95响应时间 | P99响应时间 | 成功率 |
|---------|------------|------------|------------|--------|
| 用户登录 | XXms | XXms | XXms | XX% |
| ... | ... | ... | ... | ... |

### 问题汇总
1. 问题描述
2. 问题描述
...
```

---

## 9. 常见问题处理

### 9.1 Token 管理

**问题**：Token 过期导致测试失败

**解决方案**：
1. 在集合级别添加 Pre-request Script，自动刷新 Token
2. 使用 Postman 的 Token 自动刷新功能
3. 设置 Token 过期时间检查

**示例脚本：**
```javascript
// 检查Token是否过期，如果过期则重新登录
var token = pm.environment.get("token");
if (!token) {
    // 执行登录请求
    pm.sendRequest({
        url: pm.environment.get("base_url") + "/api/auth/login",
        method: 'POST',
        header: {'Content-Type': 'application/json'},
        body: {
            mode: 'raw',
            raw: JSON.stringify({
                username: pm.environment.get("username"),
                password: pm.environment.get("password")
            })
        }
    }, function (err, res) {
        if (res.json().code === 200) {
            pm.environment.set("token", res.json().data.token);
        }
    });
}
```

### 9.2 测试数据依赖

**问题**：测试用例之间存在数据依赖

**解决方案**：
1. 使用环境变量存储测试数据ID
2. 在 Pre-request Script 中动态获取测试数据
3. 使用 Postman 的数据文件功能

### 9.3 并发测试

**问题**：Postman 单机并发能力有限

**解决方案**：
1. 使用 Postman Collection Runner 进行批量测试
2. 使用 Newman（Postman CLI）进行命令行批量执行
3. 结合 JMeter 进行高并发性能测试

---

## 10. 测试脚本示例

### 10.1 完整的测试脚本模板

```javascript
// ============================================
// 通用测试脚本模板
// ============================================

// 1. 状态码验证
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// 2. 响应时间验证
pm.test("Response time is acceptable", function () {
    pm.expect(pm.response.responseTime).to.be.below(1000);
});

// 3. 响应格式验证
pm.test("Response has correct structure", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('code');
    pm.expect(jsonData).to.have.property('message');
    pm.expect(jsonData).to.have.property('data');
});

// 4. 业务逻辑验证
pm.test("Business logic is correct", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
    // 添加具体的业务逻辑验证
});

// 5. 性能指标记录
pm.test("Performance metrics", function () {
    var responseTime = pm.response.responseTime;
    console.log("Response Time: " + responseTime + "ms");
    
    // 记录到环境变量（用于后续分析）
    var metrics = pm.environment.get("performance_metrics") || "[]";
    var metricsArray = JSON.parse(metrics);
    metricsArray.push({
        request: pm.info.requestName,
        responseTime: responseTime,
        timestamp: new Date().toISOString()
    });
    pm.environment.set("performance_metrics", JSON.stringify(metricsArray));
});
```

### 10.2 错误处理测试脚本

```javascript
// 错误场景测试
pm.test("Error handling is correct", function () {
    var jsonData = pm.response.json();
    
    if (pm.response.code !== 200) {
        // 验证错误响应格式
        pm.expect(jsonData).to.have.property('code');
        pm.expect(jsonData).to.have.property('message');
        pm.expect(jsonData.code).to.not.eql(200);
    }
});
```

---

## 11. Postman Collection 导出和共享

### 11.1 导出 Collection

1. 在 Postman 中选择集合
2. 点击 "..." → "Export"
3. 选择导出格式（推荐 JSON v2.1）
4. 保存为 `CPM_API_Collection.json`

### 11.2 导出环境变量

1. 选择环境
2. 点击 "..." → "Export"
3. 保存为 `CPM_Environment.json`

### 11.3 共享 Collection

1. 使用 Postman Workspace 共享
2. 导出 JSON 文件共享
3. 使用 Postman API 共享

---

## 12. 持续集成集成

### 12.1 使用 Newman 执行测试

**安装 Newman：**
```bash
npm install -g newman
```

**执行测试：**
```bash
newman run CPM_API_Collection.json \
  -e CPM_Environment.json \
  -r html,json \
  --reporter-html-export report.html
```

### 12.2 CI/CD 集成示例

**Jenkins Pipeline：**
```groovy
stage('API Tests') {
    steps {
        sh '''
            npm install -g newman
            newman run CPM_API_Collection.json \
              -e CPM_Environment.json \
              -r html,json \
              --reporter-html-export report.html
        '''
        publishHTML([
            reportName: 'Postman Test Report',
            reportDir: '.',
            reportFiles: 'report.html',
            keepAll: true
        ])
    }
}
```

---

## 附录

### A. 测试账号清单

| 用户名 | 密码 | 角色 | 用途 |
|--------|------|------|------|
| admin | admin123 | ADMIN | 管理员功能测试 |
| testuser | 123456 | USER | 普通用户功能测试 |

### B. 接口响应时间要求汇总

| 接口类型 | P50 | P95 | P99 | 最大 |
|---------|-----|-----|-----|------|
| 认证类 | ≤ 200ms | ≤ 500ms | ≤ 1000ms | ≤ 2000ms |
| 查询类 | ≤ 300ms | ≤ 800ms | ≤ 1500ms | ≤ 3000ms |
| 搜索类 | ≤ 500ms | ≤ 1500ms | ≤ 3000ms | ≤ 5000ms |
| 文件上传 | ≤ 2000ms | ≤ 5000ms | ≤ 10000ms | ≤ 20000ms |
| 统计类 | ≤ 1000ms | ≤ 3000ms | ≤ 5000ms | ≤ 10000ms |

### C. 错误码参考

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，Token无效或已过期 |
| 403 | 禁止访问，权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

**文档版本：** v1.0.0  
**最后更新：** 2025年1月  
**维护者：** 测试团队
