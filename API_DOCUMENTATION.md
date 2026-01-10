# 社区人口管理系统 API 接口文档

## 目录

- [1. 概述](#1-概述)
- [2. 基础信息](#2-基础信息)
- [3. 统一响应格式](#3-统一响应格式)
- [4. 认证管理](#4-认证管理)
- [5. 用户管理](#5-用户管理)
- [6. 系统管理员管理](#6-系统管理员管理)
- [7. 居民管理](#7-居民管理)
- [8. 户籍管理](#8-户籍管理)
- [9. 户籍成员管理](#9-户籍成员管理)
- [10. 错误码说明](#10-错误码说明)

---

## 1. 概述

社区人口管理系统（Community Population Management System，CPM）是一个基于 Spring Boot + MyBatis-Plus + Redis 开发的社区人口信息管理平台。

**技术栈：**

- Spring Boot 2.7.6
- MyBatis-Plus 3.5.3
- Redis
- JWT 认证
- Knife4j (Swagger) 3.0.3

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

### 5.4 修改密码

**接口地址：** `POST /api/user/change-password`

**接口说明：** 修改当前登录用户的密码

**是否需要认证：** 是

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码 |

**请求示例：**
```json
{
  "oldPassword": "123456",
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

### 5.5 分页查询用户列表

**接口地址：** `GET /api/user/list`

**接口说明：** 管理员功能，分页查询所有用户

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

### 5.6 根据ID获取用户

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

### 5.7 更新用户状态

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

**接口说明：** 分页查询所有管理员信息

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

### 7.3 更新居民信息

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

### 7.4 分页查询居民列表

**接口地址：** `GET /api/resident/list`

**接口说明：** 分页查询所有居民信息

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

### 7.5 根据ID获取居民

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

### 7.6 根据身份证号查询居民

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

**接口说明：** 分页查询所有户籍信息

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

**接口说明：** 分页查询指定户籍的所有成员

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
        "relationship": "户主"
      },
      {
        "id": 2,
        "householdId": 1,
        "residentId": 2,
        "relationship": "配偶"
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

## 10. 错误码说明

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

**文档版本：** v1.0.0  
**最后更新：** 2024年  
**维护者：** wuzuhao

