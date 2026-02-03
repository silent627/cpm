# 社区人口管理系统 API 接口文档

## 目录

- [1. 概述](#1-概述)
- [2. 基础信息](#2-基础信息)
- [3. 统一响应格式](#3-统一响应格式)
- [4. 认证管理](#4-认证管理)
- [5. 用户管理](#5-用户管理)
  - [5.1 用户注册](#51-用户注册)
  - [5.2 获取当前用户信息](#52-获取当前用户信息)
  - [5.3 更新用户信息](#53-更新用户信息)
  - [5.4 发送修改密码验证码](#54-发送修改密码验证码)
  - [5.5 修改密码](#55-修改密码)
  - [5.6 获取所有用户列表](#56-获取所有用户列表)
  - [5.7 分页查询用户列表](#57-分页查询用户列表)
  - [5.8 根据ID获取用户](#58-根据id获取用户)
  - [5.9 更新用户信息（管理员，根据ID）](#59-更新用户信息管理员根据id)
  - [5.10 删除用户](#510-删除用户)
  - [5.11 更新用户状态](#511-更新用户状态)
  - [5.12 批量删除用户](#512-批量删除用户)
  - [5.13 导出用户列表](#513-导出用户列表)
- [6. 系统管理员管理](#6-系统管理员管理)
- [7. 居民管理](#7-居民管理)
- [8. 户籍管理](#8-户籍管理)
- [9. 户籍成员管理](#9-户籍成员管理)
- [10. 文件上传管理](#10-文件上传管理)
- [11. 通知服务](#11-通知服务)
- [12. 验证码管理](#12-验证码管理)
- [13. 搜索服务](#13-搜索服务)
  - [13.1 搜索居民信息](#131-搜索居民信息)
  - [13.2 搜索户籍信息](#132-搜索户籍信息)
  - [13.3 搜索用户信息](#133-搜索用户信息)
  - [13.4 搜索管理员信息](#134-搜索管理员信息)
  - [13.5 搜索户籍成员信息](#135-搜索户籍成员信息)
  - [13.6 重建搜索索引](#136-重建搜索索引)
  - [13.7 按ID查询文档](#137-按id查询文档)
  - [13.8 条件查询列表](#138-条件查询列表)
  - [13.9 条件分页查询](#139-条件分页查询)
- [14. 统计服务](#14-统计服务)
- [15. 行政区划管理](#15-行政区划管理)
- [16. 错误码说明](#16-错误码说明)

---

## 1. 概述

社区人口管理系统（Community Population Management System，CPM）是一个基于 Spring Cloud 微服务架构开发的社区人口信息管理平台。

**技术栈：**

- Spring Boot 2.7.6
- Spring Cloud Gateway（API网关）
- MyBatis-Plus 3.5.3
- Redis（缓存和Token存储）
- Elasticsearch（全文搜索）
- JWT 认证
- Knife4j (Swagger) 3.0.3

**微服务架构：**

- **cpm-user-service**: 用户服务（认证、用户管理、管理员管理、行政区划）
- **cpm-resident-service**: 居民服务
- **cpm-household-service**: 户籍服务（户籍管理、户籍成员管理）
- **cpm-file-service**: 文件服务（文件上传、文件管理）
- **cpm-notification-service**: 通知服务（邮件、验证码）
- **cpm-search-service**: 搜索服务（Elasticsearch全文检索）
- **cpm-statistics-service**: 统计服务（数据统计）

---

## 2. 基础信息

### 2.1 基础URL

```
开发环境: http://localhost:8080
生产环境: 根据实际部署情况
```

### 2.2 认证方式

系统使用 JWT (JSON Web Token) 进行身份认证。

**请求头格式：**
```
Authorization: Bearer {token}
```

**获取Token：**
- 通过登录接口获取 Token
- Token 有效期为 24 小时（86400000 毫秒）

### 2.3 接口访问

- **Swagger 文档地址：** `http://localhost:8080/doc.html`
- **原生 Swagger UI：** `http://localhost:8080/swagger-ui.html`

---

## 3. 统一响应格式

所有接口统一使用 `Result<T>` 格式返回数据。

### 响应结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 响应消息 |
| data | Object | 响应数据，泛型类型 |

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin"
  }
}
```

**失败响应：**
```json
{
  "code": 500,
  "message": "用户名或密码错误",
  "data": null
}
```

---

## 4. 认证管理

### 4.1 用户登录

**接口地址：** `POST /api/auth/login`

**接口说明：** 使用用户名和密码登录，返回JWT Token

**是否需要认证：** 否

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**请求示例：**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 1,
    "username": "admin",
    "role": "ADMIN",
    "realName": "系统管理员"
  }
}
```

---

### 4.2 用户登出

**接口地址：** `POST /api/auth/logout`

**接口说明：** 退出登录，清除Token

**是否需要认证：** 是

**请求头：**
```
Authorization: Bearer {token}
```

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

---

### 4.3 发送忘记密码验证码

**接口地址：** `POST /api/auth/forget-password/send-code`

**接口说明：** 向指定邮箱发送验证码，用于重置密码

**是否需要认证：** 否

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | 是 | 邮箱地址 |

**请求示例：**
```json
{
  "email": "user@example.com"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "验证码已发送，请查收邮件",
  "data": null
}
```

**错误响应：**
```json
{
  "code": 500,
  "message": "发送过于频繁，请60秒后再试",
  "data": null
}
```

---

### 4.4 验证忘记密码验证码

**接口地址：** `POST /api/auth/forget-password/verify-code`

**接口说明：** 验证邮箱验证码是否正确（仅验证，不删除验证码）

**是否需要认证：** 否

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | 是 | 邮箱地址 |
| code | String | 是 | 验证码 |

**请求示例：**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "验证码正确",
  "data": null
}
```

---

### 4.5 重置密码（忘记密码）

**接口地址：** `POST /api/auth/forget-password/reset`

**接口说明：** 通过邮箱验证码重置密码

**是否需要认证：** 否

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | 是 | 邮箱地址 |
| code | String | 是 | 验证码 |
| newPassword | String | 是 | 新密码 |

**请求示例：**
```json
{
  "email": "user@example.com",
  "code": "123456",
  "newPassword": "newpassword123"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

---

## 5. 用户管理

### 5.1 用户注册

**接口地址：** `POST /api/user/register`

**接口说明：** 新用户注册，无需登录

**是否需要认证：** 否

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名（唯一） |
| password | String | 是 | 密码 |
| realName | String | 否 | 真实姓名 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| role | String | 否 | 角色，默认USER |

**请求示例：**
```json
{
  "username": "testuser",
  "password": "123456",
  "realName": "测试用户",
  "phone": "13800138000",
  "email": "test@example.com"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 14,
    "username": "testuser",
    "realName": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com",
    "role": "USER",
    "status": 1
  }
}
```

---

### 5.2 获取当前用户信息

**接口地址：** `GET /api/user/info`

**接口说明：** 获取当前登录用户的信息

**是否需要认证：** 是

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "phone": "13800138000",
    "email": "admin@cpm.com",
    "role": "ADMIN",
    "status": 1
  }
}
```

---

### 5.3 更新用户信息

**接口地址：** `PUT /api/user/update`

**接口说明：** 更新当前登录用户的信息

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| realName | String | 否 | 真实姓名 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |

**请求示例：**
```json
{
  "realName": "新姓名",
  "phone": "13900139000",
  "email": "newemail@example.com"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 5.4 发送修改密码验证码

**接口地址：** `POST /api/user/change-password/send-code`

**接口说明：** 向当前用户邮箱发送验证码，用于修改密码

**是否需要认证：** 是

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "验证码已发送，请查收邮件",
  "data": null
}
```

---

### 5.5 修改密码

**接口地址：** `POST /api/user/change-password`

**接口说明：** 修改当前登录用户的密码，支持两种方式：1. 通过旧密码验证 2. 通过邮箱验证码验证

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | String | 否 | 旧密码（与emailCode二选一） |
| emailCode | String | 否 | 邮箱验证码（与oldPassword二选一） |
| newPassword | String | 是 | 新密码 |

**请求示例（使用旧密码）：**
```json
{
  "oldPassword": "123456",
  "newPassword": "newpassword123"
}
```

**请求示例（使用邮箱验证码）：**
```json
{
  "emailCode": "123456",
  "newPassword": "newpassword123"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

---

### 5.6 获取所有用户列表

**接口地址：** `GET /api/user/all`

**接口说明：** 获取所有用户信息，用于搜索服务索引同步（内部接口）

**是否需要认证：** 是

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "phone": "13800138000",
      "email": "admin@cpm.com",
      "role": "ADMIN",
      "status": 1
    }
  ]
}
```

---

### 5.7 分页查询用户列表

**接口地址：** `GET /api/user/list`

**接口说明：** 管理员功能，使用Elasticsearch全文检索分页查询所有用户

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Integer | 否 | 当前页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| username | String | 否 | 用户名（模糊查询） |
| role | String | 否 | 角色（USER/ADMIN） |

**请求示例：**
```
GET /api/user/list?current=1&size=10&username=admin&role=ADMIN
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "phone": "13800138000",
        "email": "admin@cpm.com",
        "role": "ADMIN",
        "status": 1
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 5.8 根据ID获取用户

**接口地址：** `GET /api/user/{id}`

**接口说明：** 管理员功能，根据用户ID获取用户信息

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "phone": "13800138000",
    "email": "admin@cpm.com",
    "role": "ADMIN",
    "status": 1
  }
}
```

---

### 5.9 更新用户信息（管理员，根据ID）

**接口地址：** `PUT /api/user/update/{id}`

**接口说明：** 管理员功能，根据用户ID更新用户信息

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| realName | String | 否 | 真实姓名 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |

**请求示例：**
```json
{
  "realName": "新姓名",
  "phone": "13900139000",
  "email": "newemail@example.com"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 5.10 删除用户

**接口地址：** `DELETE /api/user/{id}`

**接口说明：** 管理员功能，逻辑删除用户

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**响应示例：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 5.11 更新用户状态

**接口地址：** `PUT /api/user/status/{id}`

**接口说明：** 管理员功能，启用或禁用用户

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | 是 | 状态：0-禁用, 1-启用 |

**请求示例：**
```json
{
  "status": 0
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

---

### 5.12 批量删除用户

**接口地址：** `POST /api/user/batch/delete`

**接口说明：** 管理员功能，批量删除用户

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<Long> | 是 | 用户ID列表 |

**请求示例：**
```json
{
  "ids": [1, 2, 3]
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "批量删除成功",
  "data": 3
}
```

---

### 5.13 导出用户列表

**接口地址：** `GET /api/user/export`

**接口说明：** 管理员功能，导出用户列表为Excel文件，支持筛选条件

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 否 | 用户名（模糊查询） |
| role | String | 否 | 角色（USER/ADMIN） |

**请求示例：**
```
GET /api/user/export?username=admin&role=ADMIN
```

**响应：** 返回Excel文件流

---

## 6. 系统管理员管理

### 6.1 获取当前管理员信息

**接口地址：** `GET /api/admin/info`

**接口说明：** 获取当前登录管理员的信息

**是否需要认证：** 是（需要管理员权限）

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "admin": {
      "id": 1,
      "userId": 1,
      "adminNo": "ADMIN001",
      "department": "系统管理部",
      "position": "系统管理员",
      "remark": "默认系统管理员"
    },
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "phone": "13800138000",
      "email": "admin@cpm.com",
      "role": "ADMIN"
    }
  }
}
```

---

### 6.2 创建管理员

**接口地址：** `POST /api/admin/create`

**接口说明：** 创建新的系统管理员账号

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| realName | String | 否 | 真实姓名 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| adminNo | String | 否 | 管理员编号 |
| department | String | 否 | 部门 |
| position | String | 否 | 职位 |
| remark | String | 否 | 备注 |

**请求示例：**
```json
{
  "username": "admin2",
  "password": "123456",
  "realName": "张管理员",
  "phone": "13800138001",
  "email": "admin2@cpm.com",
  "adminNo": "ADMIN002",
  "department": "社区管理部",
  "position": "社区管理员",
  "remark": "负责社区日常管理"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "userId": 2,
    "adminNo": "ADMIN002",
    "department": "社区管理部",
    "position": "社区管理员"
  }
}
```

---

### 6.3 更新管理员信息

**接口地址：** `PUT /api/admin/update`

**接口说明：** 更新当前登录管理员的信息

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| adminNo | String | 否 | 管理员编号 |
| department | String | 否 | 部门 |
| position | String | 否 | 职位 |
| remark | String | 否 | 备注 |

**请求示例：**
```json
{
  "department": "新部门",
  "position": "新职位"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 6.4 分页查询管理员列表

**接口地址：** `GET /api/admin/list`

**接口说明：** 使用Elasticsearch全文检索分页查询所有管理员信息

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Integer | 否 | 当前页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| adminNo | String | 否 | 管理员编号（模糊查询） |
| department | String | 否 | 部门（模糊查询） |

**请求示例：**
```
GET /api/admin/list?current=1&size=10&department=系统管理部
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "adminNo": "ADMIN001",
        "department": "系统管理部",
        "position": "系统管理员"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 6.5 根据ID获取管理员

**接口地址：** `GET /api/admin/{id}`

**接口说明：** 根据管理员ID获取详细信息

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 管理员ID |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "adminNo": "ADMIN001",
    "department": "系统管理部",
    "position": "系统管理员",
    "remark": "默认系统管理员"
  }
}
```

---

### 6.6 获取所有管理员列表

**接口地址：** `GET /api/admin/all`

**接口说明：** 获取所有管理员信息，用于搜索服务索引同步（内部接口）

**是否需要认证：** 是

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "adminNo": "ADMIN001",
      "department": "系统管理部",
      "position": "系统管理员"
    }
  ]
}
```

---

### 6.7 导出管理员列表

**接口地址：** `GET /api/admin/export`

**接口说明：** 导出管理员列表为Excel文件，支持筛选条件

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| adminNo | String | 否 | 管理员编号（模糊查询） |
| department | String | 否 | 部门（模糊查询） |

**请求示例：**
```
GET /api/admin/export?department=系统管理部
```

**响应：** 返回Excel文件流

---

## 7. 居民管理

### 7.1 获取当前居民信息

**接口地址：** `GET /api/resident/info`

**接口说明：** 获取当前登录居民的信息

**是否需要认证：** 是

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "resident": {
      "id": 1,
      "userId": 4,
      "realName": "张三",
      "idCard": "110101199001011234",
      "gender": 1,
      "birthDate": "1990-01-01",
      "nationality": "汉族",
      "registeredAddress": "北京市东城区XX街道XX号",
      "currentAddress": "北京市东城区XX街道XX号",
      "occupation": "软件工程师",
      "education": "本科",
      "maritalStatus": 1
    },
    "user": {
      "id": 4,
      "username": "zhangsan",
      "realName": "张三",
      "phone": "13800138010",
      "email": "zhangsan@example.com",
      "role": "USER"
    }
  }
}
```

---

### 7.2 创建居民

**接口地址：** `POST /api/resident/create`

**接口说明：** 创建新的居民账号和信息

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| realName | String | 是 | 真实姓名 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| idCard | String | 是 | 身份证号（唯一） |
| gender | Integer | 否 | 性别：0-女, 1-男 |
| birthDate | String | 否 | 出生日期（格式：yyyy-MM-dd） |
| nationality | String | 否 | 民族 |
| registeredAddress | String | 否 | 户籍地址 |
| currentAddress | String | 否 | 现居住地址 |
| occupation | String | 否 | 职业 |
| education | String | 否 | 文化程度 |
| maritalStatus | Integer | 否 | 婚姻状况：0-未婚, 1-已婚, 2-离异, 3-丧偶 |
| contactPhone | String | 否 | 联系电话 |
| emergencyContact | String | 否 | 紧急联系人 |
| emergencyPhone | String | 否 | 紧急联系人电话 |
| remark | String | 否 | 备注 |

**请求示例：**
```json
{
  "username": "zhangsan",
  "password": "123456",
  "realName": "张三",
  "phone": "13800138010",
  "email": "zhangsan@example.com",
  "idCard": "110101199001011234",
  "gender": 1,
  "birthDate": "1990-01-01",
  "nationality": "汉族",
  "registeredAddress": "北京市东城区XX街道XX号",
  "currentAddress": "北京市东城区XX街道XX号",
  "occupation": "软件工程师",
  "education": "本科",
  "maritalStatus": 1,
  "contactPhone": "13800138010",
  "emergencyContact": "张父",
  "emergencyPhone": "13800138020"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "userId": 4,
    "realName": "张三",
    "idCard": "110101199001011234",
    "gender": 1,
    "birthDate": "1990-01-01",
    "nationality": "汉族"
  }
}
```

---

### 7.3 更新居民信息（根据ID）

**接口地址：** `PUT /api/resident/update/{id}`

**接口说明：** 根据居民ID更新居民信息

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 居民ID |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| realName | String | 否 | 真实姓名 |
| idCard | String | 否 | 身份证号（唯一） |
| gender | Integer | 否 | 性别 |
| birthDate | String | 否 | 出生日期 |
| nationality | String | 否 | 民族 |
| registeredAddress | String | 否 | 户籍地址 |
| currentAddress | String | 否 | 现居住地址 |
| occupation | String | 否 | 职业 |
| education | String | 否 | 文化程度 |
| maritalStatus | Integer | 否 | 婚姻状况 |
| contactPhone | String | 否 | 联系电话 |
| emergencyContact | String | 否 | 紧急联系人 |
| emergencyPhone | String | 否 | 紧急联系人电话 |
| remark | String | 否 | 备注 |

**请求示例：**
```json
{
  "currentAddress": "新地址",
  "occupation": "新职业"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 7.4 更新当前居民信息

**接口地址：** `PUT /api/resident/update`

**接口说明：** 更新当前登录居民的信息

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| realName | String | 否 | 真实姓名 |
| gender | Integer | 否 | 性别 |
| birthDate | String | 否 | 出生日期 |
| nationality | String | 否 | 民族 |
| registeredAddress | String | 否 | 户籍地址 |
| currentAddress | String | 否 | 现居住地址 |
| occupation | String | 否 | 职业 |
| education | String | 否 | 文化程度 |
| maritalStatus | Integer | 否 | 婚姻状况 |
| contactPhone | String | 否 | 联系电话 |
| emergencyContact | String | 否 | 紧急联系人 |
| emergencyPhone | String | 否 | 紧急联系人电话 |
| remark | String | 否 | 备注 |

**请求示例：**
```json
{
  "currentAddress": "新地址",
  "occupation": "新职业"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 7.5 分页查询居民列表

**接口地址：** `GET /api/resident/list`

**接口说明：** 使用Elasticsearch全文检索分页查询所有居民信息

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Integer | 否 | 当前页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| realName | String | 否 | 真实姓名（模糊查询） |
| idCard | String | 否 | 身份证号（模糊查询） |
| currentAddress | String | 否 | 现居住地址（模糊查询） |

**请求示例：**
```
GET /api/resident/list?current=1&size=10&realName=张三
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 4,
        "realName": "张三",
        "idCard": "110101199001011234",
        "gender": 1,
        "birthDate": "1990-01-01",
        "nationality": "汉族",
        "currentAddress": "北京市东城区XX街道XX号",
        "occupation": "软件工程师"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 7.6 根据ID获取居民

**接口地址：** `GET /api/resident/{id}`

**接口说明：** 根据居民ID获取详细信息

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 居民ID |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 4,
    "realName": "张三",
    "idCard": "110101199001011234",
    "gender": 1,
    "birthDate": "1990-01-01",
    "nationality": "汉族",
    "registeredAddress": "北京市东城区XX街道XX号",
    "currentAddress": "北京市东城区XX街道XX号",
    "occupation": "软件工程师",
    "education": "本科",
    "maritalStatus": 1
  }
}
```

---

### 7.7 获取所有居民列表

**接口地址：** `GET /api/resident/all`

**接口说明：** 获取所有居民信息，用于统计服务（内部接口）

**是否需要认证：** 是

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 4,
      "realName": "张三",
      "idCard": "110101199001011234",
      "gender": 1,
      "birthDate": "1990-01-01"
    }
  ]
}
```

---

### 7.8 根据身份证号查询居民

**接口地址：** `GET /api/resident/idCard/{idCard}`

**接口说明：** 根据身份证号查询居民信息

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| idCard | String | 是 | 身份证号 |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 4,
    "realName": "张三",
    "idCard": "110101199001011234",
    "gender": 1,
    "birthDate": "1990-01-01"
  }
}
```

---

### 7.9 删除居民

**接口地址：** `DELETE /api/resident/{id}`

**接口说明：** 逻辑删除居民信息

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 居民ID |

**响应示例：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 7.10 批量删除居民

**接口地址：** `POST /api/resident/batch/delete`

**接口说明：** 批量删除居民信息

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<Long> | 是 | 居民ID列表 |

**请求示例：**
```json
{
  "ids": [1, 2, 3]
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "批量删除成功",
  "data": 3
}
```

---

### 7.11 导出居民列表

**接口地址：** `GET /api/resident/export`

**接口说明：** 导出居民列表为Excel文件，支持筛选条件

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| realName | String | 否 | 真实姓名（模糊查询） |
| idCard | String | 否 | 身份证号（模糊查询） |
| currentAddress | String | 否 | 现居住地址（模糊查询） |

**请求示例：**
```
GET /api/resident/export?realName=张三
```

**响应：** 返回Excel文件流

---

### 7.12 下载导入模板

**接口地址：** `GET /api/resident/import/template`

**接口说明：** 下载居民信息导入Excel模板

**是否需要认证：** 是（需要管理员权限）

**请求参数：** 无

**响应：** 返回Excel模板文件流

---

### 7.13 导入居民信息

**接口地址：** `POST /api/resident/import`

**接口说明：** 从Excel文件批量导入居民信息

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | Excel文件 |

**请求示例：**
```
POST /api/resident/import
Content-Type: multipart/form-data

file: [Excel文件]
```

**响应示例：**
```json
{
  "code": 200,
  "message": "导入完成",
  "data": {
    "total": 100,
    "success": 95,
    "failed": 5,
    "errors": [
      "第3行：身份证号格式不正确",
      "第5行：身份证号已存在"
    ]
  }
}
```

---

## 8. 户籍管理

### 8.1 创建户籍

**接口地址：** `POST /api/household/create`

**接口说明：** 创建新的户籍信息

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| headId | Long | 否 | 户主ID（关联resident表） |
| headName | String | 否 | 户主姓名 |
| headIdCard | String | 否 | 户主身份证号 |
| householdNo | String | 否 | 户籍编号（不填则自动生成） |
| address | String | 是 | 户籍地址 |
| householdType | Integer | 否 | 户别：1-家庭户, 2-集体户，默认1 |
| memberCount | Integer | 否 | 户人数，默认0 |
| contactPhone | String | 否 | 联系电话 |
| moveInDate | String | 否 | 迁入日期（格式：yyyy-MM-dd HH:mm:ss） |
| moveInReason | String | 否 | 迁入原因 |
| status | Integer | 否 | 状态：0-迁出, 1-正常，默认1 |
| remark | String | 否 | 备注 |

**请求示例：**
```json
{
  "headId": 1,
  "headName": "张三",
  "headIdCard": "110101199001011234",
  "householdNo": "HH2024001",
  "address": "北京市东城区XX街道XX号",
  "householdType": 1,
  "memberCount": 3,
  "contactPhone": "13800138010",
  "moveInDate": "2010-01-01 00:00:00",
  "moveInReason": "购房迁入",
  "status": 1
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "headId": 1,
    "headName": "张三",
    "headIdCard": "110101199001011234",
    "householdNo": "HH2024001",
    "address": "北京市东城区XX街道XX号",
    "householdType": 1,
    "memberCount": 3,
    "status": 1
  }
}
```

---

### 8.2 更新户籍信息

**接口地址：** `PUT /api/household/update/{id}`

**接口说明：** 更新指定户籍的信息

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 户籍ID |

**请求参数：** 同创建户籍的参数（除householdNo外）

**请求示例：**
```json
{
  "address": "新地址",
  "memberCount": 4
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 8.3 分页查询户籍列表

**接口地址：** `GET /api/household/list`

**接口说明：** 使用Elasticsearch全文检索分页查询所有户籍信息

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Integer | 否 | 当前页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| householdNo | String | 否 | 户籍编号（模糊查询） |
| headName | String | 否 | 户主姓名（模糊查询） |
| address | String | 否 | 户籍地址（模糊查询） |
| status | Integer | 否 | 状态：0-迁出, 1-正常 |

**请求示例：**
```
GET /api/household/list?current=1&size=10&headName=张三&status=1
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "headId": 1,
        "headName": "张三",
        "headIdCard": "110101199001011234",
        "householdNo": "HH2024001",
        "address": "北京市东城区XX街道XX号",
        "householdType": 1,
        "memberCount": 3,
        "status": 1
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 8.4 根据ID获取户籍

**接口地址：** `GET /api/household/{id}`

**接口说明：** 根据户籍ID获取详细信息

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 户籍ID |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "headId": 1,
    "headName": "张三",
    "headIdCard": "110101199001011234",
    "householdNo": "HH2024001",
    "address": "北京市东城区XX街道XX号",
    "householdType": 1,
    "memberCount": 3,
    "contactPhone": "13800138010",
    "moveInDate": "2010-01-01T00:00:00",
    "moveInReason": "购房迁入",
    "status": 1
  }
}
```

---

### 8.5 根据户籍编号查询

**接口地址：** `GET /api/household/no/{householdNo}`

**接口说明：** 根据户籍编号查询户籍信息

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| householdNo | String | 是 | 户籍编号 |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "headId": 1,
    "headName": "张三",
    "householdNo": "HH2024001",
    "address": "北京市东城区XX街道XX号",
    "householdType": 1,
    "memberCount": 3,
    "status": 1
  }
}
```

---

### 8.6 删除户籍

**接口地址：** `DELETE /api/household/{id}`

**接口说明：** 逻辑删除指定户籍

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 户籍ID |

**响应示例：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 8.7 户籍迁出

**接口地址：** `POST /api/household/move-out/{id}`

**接口说明：** 办理户籍迁出手续

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 户籍ID |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 是 | 迁出原因 |

**请求示例：**
```json
{
  "reason": "工作调动"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "迁出成功",
  "data": null
}
```

---

### 8.8 导出户籍列表

**接口地址：** `GET /api/household/export`

**接口说明：** 导出户籍列表为Excel文件，支持筛选条件

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| householdNo | String | 否 | 户籍编号（模糊查询） |
| headName | String | 否 | 户主姓名（模糊查询） |
| address | String | 否 | 户籍地址（模糊查询） |
| status | Integer | 否 | 状态：0-迁出, 1-正常 |

**请求示例：**
```
GET /api/household/export?headName=张三&status=1
```

**响应：** 返回Excel文件流

---

### 8.9 获取所有户籍列表

**接口地址：** `GET /api/household/all`

**接口说明：** 获取所有户籍信息，用于统计服务（内部接口）

**是否需要认证：** 是

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "headId": 1,
      "headName": "张三",
      "householdNo": "HH2024001",
      "address": "北京市东城区XX街道XX号",
      "householdType": 1,
      "memberCount": 3,
      "status": 1
    }
  ]
}
```

---

## 9. 户籍成员管理

### 9.1 添加成员到户籍

**接口地址：** `POST /api/household-member/add`

**接口说明：** 将居民添加到指定户籍

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| householdId | Long | 是 | 户籍ID |
| residentId | Long | 是 | 居民ID |
| relationship | String | 否 | 与户主关系：户主、配偶、子女、父母、其他 |

**请求示例：**
```json
{
  "householdId": 1,
  "residentId": 2,
  "relationship": "配偶"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 2,
    "householdId": 1,
    "residentId": 2,
    "relationship": "配偶"
  }
}
```

---

### 9.2 从户籍中移除成员

**接口地址：** `DELETE /api/household-member/remove`

**接口说明：** 从指定户籍中移除居民

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| householdId | Long | 是 | 户籍ID |
| residentId | Long | 是 | 居民ID |

**请求示例：**
```
DELETE /api/household-member/remove?householdId=1&residentId=2
```

**响应示例：**
```json
{
  "code": 200,
  "message": "移除成功",
  "data": null
}
```

---

### 9.3 查询户籍的所有成员

**接口地址：** `GET /api/household-member/list/{householdId}`

**接口说明：** 使用Elasticsearch全文检索分页查询指定户籍的所有成员，包含居民详细信息

**是否需要认证：** 是（需要管理员权限）

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| householdId | Long | 是 | 户籍ID |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Integer | 否 | 当前页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例：**
```
GET /api/household-member/list/1?current=1&size=10
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "householdId": 1,
        "residentId": 1,
        "relationship": "户主",
        "realName": "张三",
        "idCard": "110101199001011234",
        "gender": 1,
        "birthDate": "1990-01-01",
        "nationality": "汉族",
        "currentAddress": "北京市东城区XX街道XX号",
        "occupation": "软件工程师",
        "education": "本科"
      },
      {
        "id": 2,
        "householdId": 1,
        "residentId": 2,
        "relationship": "配偶",
        "realName": "李四",
        "idCard": "110101199001011235",
        "gender": 0,
        "birthDate": "1992-01-01"
      }
    ],
    "total": 2,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 9.4 获取所有户籍成员列表

**接口地址：** `GET /api/household-member/all`

**接口说明：** 获取所有户籍成员信息，用于搜索服务索引同步（内部接口）

**是否需要认证：** 是

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "householdId": 1,
      "residentId": 1,
      "relationship": "户主"
    }
  ]
}
```

---

## 10. 文件上传管理

### 10.1 上传头像

**接口地址：** `POST /api/upload/avatar`

**接口说明：** 上传头像图片，支持jpg、jpeg、png、gif、bmp格式，最大10MB

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | 头像文件 |

**请求示例：**
```
POST /api/upload/avatar
Content-Type: multipart/form-data

file: [图片文件]
```

**响应示例：**
```json
{
  "code": 200,
  "message": "上传成功",
  "data": "/uploads/avatar/2024/01/01/abc123.jpg"
}
```

---

### 10.2 上传身份证照片

**接口地址：** `POST /api/upload/idCard`

**接口说明：** 上传身份证照片，支持jpg、jpeg、png、gif、bmp格式，最大10MB

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | 身份证照片文件 |

**请求示例：**
```
POST /api/upload/idCard
Content-Type: multipart/form-data

file: [图片文件]
```

**响应示例：**
```json
{
  "code": 200,
  "message": "上传成功",
  "data": "/uploads/idCard/2024/01/01/abc123.jpg"
}
```

---

### 10.3 通用文件上传

**接口地址：** `POST /api/upload/file`

**接口说明：** 通用文件上传接口，支持jpg、jpeg、png、gif、bmp格式，最大10MB

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | 文件 |
| subPath | String | 否 | 子路径，默认files |

**请求示例：**
```
POST /api/upload/file?subPath=documents
Content-Type: multipart/form-data

file: [文件]
```

**响应示例：**
```json
{
  "code": 200,
  "message": "上传成功",
  "data": "/uploads/files/documents/2024/01/01/abc123.pdf"
}
```

---

### 10.4 删除文件

**接口地址：** `DELETE /api/upload/file`

**接口说明：** 根据文件URL删除文件

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| url | String | 是 | 文件URL |

**请求示例：**
```
DELETE /api/upload/file?url=/uploads/avatar/2024/01/01/abc123.jpg
```

**响应示例：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 10.5 批量删除文件

**接口地址：** `POST /api/upload/batch/delete`

**接口说明：** 根据文件URL列表批量删除文件

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| urls | List<String> | 是 | 文件URL列表 |

**请求示例：**
```json
{
  "urls": [
    "/uploads/avatar/2024/01/01/abc123.jpg",
    "/uploads/idCard/2024/01/01/def456.jpg"
  ]
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "批量删除完成",
  "data": 2
}
```

---

### 10.6 处理文件更新

**接口地址：** `POST /api/upload/handle-update`

**接口说明：** 如果新文件URL与旧文件URL不同，则删除旧文件

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldUrl | String | 否 | 旧文件URL |
| newUrl | String | 否 | 新文件URL |

**请求示例：**
```
POST /api/upload/handle-update?oldUrl=/uploads/avatar/old.jpg&newUrl=/uploads/avatar/new.jpg
```

**响应示例：**
```json
{
  "code": 200,
  "message": "已删除旧文件",
  "data": true
}
```

---

## 11. 通知服务

### 11.1 发送邮件

**接口地址：** `POST /api/notification/email/send`

**接口说明：** 发送普通邮件

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| to | String | 是 | 收件人邮箱 |
| subject | String | 是 | 邮件主题 |
| content | String | 是 | 邮件内容 |

**请求示例：**
```
POST /api/notification/email/send?to=user@example.com&subject=测试邮件&content=这是一封测试邮件
```

**响应示例：**
```json
{
  "code": 200,
  "message": "邮件发送成功",
  "data": null
}
```

---

### 11.2 发送邮箱验证码

**接口地址：** `POST /api/notification/email/code/send`

**接口说明：** 发送邮箱验证码，60秒内只能发送一次

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | 是 | 邮箱地址 |
| type | String | 否 | 验证码类型，默认forget |

**请求示例：**
```
POST /api/notification/email/code/send?email=user@example.com&type=forget
```

**响应示例：**
```json
{
  "code": 200,
  "message": "验证码发送成功",
  "data": null
}
```

---

### 11.3 验证邮箱验证码

**接口地址：** `POST /api/notification/email/code/validate`

**接口说明：** 验证邮箱验证码，验证成功后删除验证码

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | 是 | 邮箱地址 |
| code | String | 是 | 验证码 |
| type | String | 否 | 验证码类型，默认forget |

**请求示例：**
```
POST /api/notification/email/code/validate?email=user@example.com&code=123456&type=forget
```

**响应示例：**
```json
{
  "code": 200,
  "message": "验证码验证成功",
  "data": null
}
```

---

### 11.4 仅验证邮箱验证码

**接口地址：** `POST /api/notification/email/code/verify`

**接口说明：** 仅验证邮箱验证码，不删除验证码（用于中间步骤验证）

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | 是 | 邮箱地址 |
| code | String | 是 | 验证码 |
| type | String | 否 | 验证码类型，默认forget |

**请求示例：**
```
POST /api/notification/email/code/verify?email=user@example.com&code=123456&type=forget
```

**响应示例：**
```json
{
  "code": 200,
  "message": "验证码验证成功",
  "data": null
}
```

---

### 11.5 检查是否可以发送验证码

**接口地址：** `GET /api/notification/email/code/can-send`

**接口说明：** 检查是否可以发送验证码（60秒间隔限制）

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | 是 | 邮箱地址 |
| type | String | 否 | 验证码类型，默认forget |

**请求示例：**
```
GET /api/notification/email/code/can-send?email=user@example.com&type=forget
```

**响应示例：**
```json
{
  "code": 200,
  "message": "可以发送",
  "data": true
}
```

---

### 11.6 获取剩余等待时间

**接口地址：** `GET /api/notification/email/code/remaining`

**接口说明：** 获取剩余等待时间（秒）

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | 是 | 邮箱地址 |
| type | String | 否 | 验证码类型，默认forget |

**请求示例：**
```
GET /api/notification/email/code/remaining?email=user@example.com&type=forget
```

**响应示例：**
```json
{
  "code": 200,
  "message": "剩余等待时间",
  "data": 45
}
```

---

## 12. 验证码管理

### 12.1 生成图形验证码

**接口地址：** `GET /api/captcha/generate`

**接口说明：** 生成图形验证码，返回验证码图片和key

**是否需要认证：** 否

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "key": "captcha_abc123",
    "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
  }
}
```

---

### 12.2 验证图形验证码

**接口地址：** `POST /api/captcha/validate`

**接口说明：** 验证图形验证码，验证成功后删除验证码

**是否需要认证：** 否

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| key | String | 是 | 验证码key |
| code | String | 是 | 验证码 |

**请求示例：**
```
POST /api/captcha/validate?key=captcha_abc123&code=1234
```

**响应示例：**
```json
{
  "code": 200,
  "message": "验证码验证成功",
  "data": null
}
```

---

## 13. 搜索服务

### 13.1 搜索居民信息

**接口地址：** `GET /api/search/resident`

**接口说明：** 使用Elasticsearch全文检索搜索居民信息

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | Integer | 否 | 页码（从0开始），默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例：**
```
GET /api/search/resident?keyword=张三&page=0&size=10
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "hits": [
      {
        "id": 1,
        "realName": "张三",
        "idCard": "110101199001011234",
        "currentAddress": "北京市东城区XX街道XX号"
      }
    ],
    "total": 1,
    "page": 0,
    "size": 10
  }
}
```

---

### 13.2 搜索户籍信息

**接口地址：** `GET /api/search/household`

**接口说明：** 使用Elasticsearch全文检索搜索户籍信息

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | Integer | 否 | 页码（从0开始），默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例：**
```
GET /api/search/household?keyword=HH2024001&page=0&size=10
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "hits": [
      {
        "id": 1,
        "householdNo": "HH2024001",
        "headName": "张三",
        "address": "北京市东城区XX街道XX号"
      }
    ],
    "total": 1,
    "page": 0,
    "size": 10
  }
}
```

---

### 13.3 搜索用户信息

**接口地址：** `GET /api/search/user`

**接口说明：** 使用Elasticsearch全文检索搜索用户信息

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | Integer | 否 | 页码（从0开始），默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例：**
```
GET /api/search/user?keyword=admin&page=0&size=10
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "hits": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "email": "admin@cpm.com"
      }
    ],
    "total": 1,
    "page": 0,
    "size": 10
  }
}
```

---

### 13.4 搜索管理员信息

**接口地址：** `GET /api/search/admin`

**接口说明：** 使用Elasticsearch全文检索搜索管理员信息

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | Integer | 否 | 页码（从0开始），默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例：**
```
GET /api/search/admin?keyword=ADMIN001&page=0&size=10
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "hits": [
      {
        "id": 1,
        "adminNo": "ADMIN001",
        "department": "系统管理部",
        "position": "系统管理员"
      }
    ],
    "total": 1,
    "page": 0,
    "size": 10
  }
}
```

---

### 13.5 搜索户籍成员信息

**接口地址：** `GET /api/search/household-member`

**接口说明：** 使用Elasticsearch全文检索搜索户籍成员信息

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | Integer | 否 | 页码（从0开始），默认0 |
| size | Integer | 否 | 每页数量，默认10 |
| householdId | Long | 否 | 户籍ID（可选过滤条件） |

**请求示例：**
```
GET /api/search/household-member?keyword=张三&page=0&size=10&householdId=1
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "hits": [
      {
        "id": 1,
        "householdId": 1,
        "residentId": 1,
        "relationship": "户主"
      }
    ],
    "total": 1,
    "page": 0,
    "size": 10
  }
}
```

---

### 13.6 重建搜索索引

**接口地址：** `POST /api/search/index/rebuild`

**接口说明：** 重建所有搜索索引（用于数据同步）

**是否需要认证：** 是（需要管理员权限）

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

### 13.7 按ID查询文档

**接口地址：** `GET /api/search/{index}/{id}`

**接口说明：** 根据索引名称和文档ID查询文档

**是否需要认证：** 是

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| index | String | 是 | 索引名称（如：resident, household, user, admin, household-member） |
| id | Long | 是 | 文档ID |

**请求示例：**
```
GET /api/search/resident/1
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "realName": "张三",
    "idCard": "110101199001011234"
  }
}
```

---

### 13.8 条件查询列表

**接口地址：** `POST /api/search/{index}/list`

**接口说明：** 使用ESQueryWrapper条件查询文档列表

**是否需要认证：** 是

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| index | String | 是 | 索引名称 |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| wrapper | ESQueryWrapper | 是 | 查询条件包装器（JSON格式） |

**请求示例：**
```json
{
  "eq": {
    "gender": 1
  },
  "like": {
    "realName": "张"
  }
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "realName": "张三",
      "gender": 1
    }
  ]
}
```

---

### 13.9 条件分页查询

**接口地址：** `POST /api/search/{index}/page`

**接口说明：** 使用ESQueryWrapper条件分页查询文档

**是否需要认证：** 是

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| index | String | 是 | 索引名称 |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| wrapper | ESQueryWrapper | 是 | 查询条件包装器（JSON格式） |
| current | Integer | 否 | 当前页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例：**
```json
POST /api/search/resident/page?current=1&size=10
{
  "eq": {
    "gender": 1
  }
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "realName": "张三",
        "gender": 1
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10
  }
}
```

---

## 14. 统计服务

### 14.1 获取居民年龄分布统计

**接口地址：** `GET /api/statistics/resident/age-distribution`

**接口说明：** 按年龄段统计居民数量

**是否需要认证：** 是（需要管理员权限）

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "0-18": 50,
    "19-30": 120,
    "31-45": 180,
    "46-60": 150,
    "60+": 100
  }
}
```

---

### 14.2 获取居民性别统计

**接口地址：** `GET /api/statistics/resident/gender`

**接口说明：** 统计男女居民数量

**是否需要认证：** 是（需要管理员权限）

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "male": 300,
    "female": 300
  }
}
```

---

### 14.3 获取户籍类型统计

**接口地址：** `GET /api/statistics/household/type`

**接口说明：** 统计家庭户和集体户数量

**是否需要认证：** 是（需要管理员权限）

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "family": 200,
    "collective": 50
  }
}
```

---

### 14.4 获取户籍迁入迁出趋势

**接口地址：** `GET /api/statistics/household/move-trend`

**接口说明：** 按月或年统计户籍迁入迁出趋势

**是否需要认证：** 是（需要管理员权限）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | String | 否 | 统计类型：month-月度，year-年度，默认month |

**请求示例：**
```
GET /api/statistics/household/move-trend?type=month
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "moveIn": [
      {"period": "2024-01", "count": 10},
      {"period": "2024-02", "count": 15}
    ],
    "moveOut": [
      {"period": "2024-01", "count": 5},
      {"period": "2024-02", "count": 8}
    ]
  }
}
```

---

### 14.5 获取月度数据统计

**接口地址：** `GET /api/statistics/monthly`

**接口说明：** 按月统计居民和户籍新增数量

**是否需要认证：** 是（需要管理员权限）

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "resident": [
      {"month": "2024-01", "count": 20},
      {"month": "2024-02", "count": 25}
    ],
    "household": [
      {"month": "2024-01", "count": 10},
      {"month": "2024-02", "count": 12}
    ]
  }
}
```

---

### 14.6 获取年度数据统计

**接口地址：** `GET /api/statistics/yearly`

**接口说明：** 按年统计居民和户籍新增数量

**是否需要认证：** 是（需要管理员权限）

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "resident": [
      {"year": "2023", "count": 200},
      {"year": "2024", "count": 150}
    ],
    "household": [
      {"year": "2023", "count": 100},
      {"year": "2024", "count": 80}
    ]
  }
}
```

---

## 15. 行政区划管理

### 15.1 获取所有省份

**接口地址：** `GET /api/region/provinces`

**接口说明：** 获取所有省级行政区划（省、直辖市、自治区、特别行政区）

**是否需要认证：** 是

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "code": "110000",
      "name": "北京市",
      "level": 1
    },
    {
      "code": "120000",
      "name": "天津市",
      "level": 1
    }
  ]
}
```

---

### 15.2 获取下级行政区划

**接口地址：** `GET /api/region/children/{parentCode}`

**接口说明：** 根据父级区划代码获取下级行政区划（自动适配直辖市）

**是否需要认证：** 是

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| parentCode | String | 是 | 父级区划代码 |

**请求示例：**
```
GET /api/region/children/110000
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "code": "110000",
    "name": "北京市",
    "children": [
      {
        "code": "110100",
        "name": "市辖区",
        "level": 2
      }
    ]
  }
}
```

---

### 15.3 获取数据统计信息

**接口地址：** `GET /api/region/stats`

**接口说明：** 获取行政区划数据统计信息

**是否需要认证：** 是

**请求参数：** 无

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 3000,
    "provinces": 34,
    "cities": 333,
    "districts": 2633
  }
}
```

---

## 16. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，Token无效或已过期 |
| 403 | 禁止访问，权限不足 |
| 404 | 资源不存在 |
| 405 | 请求方法不支持 |
| 500 | 服务器内部错误 |

### 常见错误响应

**Token无效或过期：**
```json
{
  "code": 401,
  "message": "Token无效或已过期",
  "data": null
}
```

**权限不足：**
```json
{
  "code": 403,
  "message": "权限不足",
  "data": null
}
```

**参数验证失败：**
```json
{
  "code": 400,
  "message": "用户名不能为空; 密码不能为空",
  "data": null
}
```

**资源不存在：**
```json
{
  "code": 404,
  "message": "用户不存在",
  "data": null
}
```

---

## 附录

### A. 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | ADMIN | 系统管理员 |
| zhangsan | 123456 | USER | 普通用户（居民） |

### B. 日期格式

- **日期格式：** `yyyy-MM-dd`（如：1990-01-01）
- **日期时间格式：** `yyyy-MM-dd HH:mm:ss`（如：2010-01-01 00:00:00）

### C. 性别枚举

- `0` - 女
- `1` - 男

### D. 婚姻状况枚举

- `0` - 未婚
- `1` - 已婚
- `2` - 离异
- `3` - 丧偶

### E. 户别枚举

- `1` - 家庭户
- `2` - 集体户

### F. 状态枚举

**用户状态：**
- `0` - 禁用
- `1` - 启用

**户籍状态：**
- `0` - 迁出
- `1` - 正常

---

**文档版本：** v2.1.0  
**最后更新：** 2025年1月  
**维护者：** wuzuhao

---

## 更新日志

### v2.1.0 (2025-01)
- 新增用户管理接口：更新用户信息（管理员，根据ID）、删除用户、批量删除用户、导出用户列表
- 新增搜索服务接口：按ID查询文档、条件查询列表、条件分页查询
- 更新所有分页查询接口说明，明确使用Elasticsearch全文检索
- 完善接口文档，确保与实际代码实现一致

### v2.0.0 (2024)
- 补充完整的API接口文档
- 新增文件上传管理模块
- 新增通知服务模块
- 新增验证码管理模块
- 新增搜索服务模块
- 新增统计服务模块
- 新增行政区划管理模块
- 补充认证管理中的忘记密码功能
- 补充用户管理中的批量操作和Excel导出功能
- 补充居民管理中的批量操作和Excel导入导出功能
- 补充户籍管理中的Excel导出功能
- 更新项目架构说明，明确微服务架构
