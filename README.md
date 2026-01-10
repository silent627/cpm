# 社区人口管理系统

基于Spring Boot + MyBatis-Plus + Redis的社区人口管理系统

## 技术栈

- **后端框架**: Spring Boot 2.7.6
- **ORM框架**: MyBatis-Plus 3.5.3
- **数据库**: MySQL 8.0+
- **缓存**: Redis
- **认证**: JWT (JSON Web Token)
- **工具类**: Hutool 5.8.16
- **构建工具**: Maven

## 功能模块

### 1. 登录验证模块
- 用户登录（JWT Token认证）
- 用户登出
- Token验证和刷新

### 2. 用户模块
- 用户注册
- 用户信息查询和更新
- 密码修改
- 用户列表查询（管理员）
- 用户状态管理

### 3. 系统管理员模块
- 管理员信息管理
- 管理员创建和查询
- 管理员列表分页查询

### 4. 居民模块
- 居民信息管理
- 居民创建和查询
- 居民列表分页查询
- 根据身份证号查询

### 5. 户籍模块
- 户籍信息管理
- 户籍创建和查询
- 户籍列表分页查询
- 户籍迁入/迁出管理
- 户籍成员管理

## 项目结构

```
src/main/java/com/wuzuhao/cpm/
├── common/          # 通用类（Result、ResultCode）
├── config/          # 配置类（Redis、MyBatis-Plus、Web）
├── controller/      # 控制器层
├── dto/            # 数据传输对象
├── entity/         # 实体类
├── exception/      # 异常处理
├── interceptor/    # 拦截器
├── mapper/         # Mapper接口
├── service/        # 服务接口
│   └── impl/       # 服务实现类
└── util/           # 工具类
```

## 数据库设计

### 主要表结构

1. **sys_user** - 用户表
2. **sys_admin** - 系统管理员表
3. **resident** - 居民表
4. **household** - 户籍表
5. **household_member** - 户籍成员关系表

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE cpm_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p cpm_db < src/main/resources/sql/init.sql
```

### 3. 配置文件

修改 `src/main/resources/application.properties` 中的数据库和Redis配置：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/cpm_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456

# Redis配置
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=123456
```

### 4. 运行项目

```bash
mvn clean install
mvn spring-boot:run
```

项目启动后，访问地址：http://localhost:8080

## API接口文档

### 认证接口

#### 登录
- **URL**: `/api/auth/login`
- **Method**: POST
- **Request Body**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
- **Response**:
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

#### 登出
- **URL**: `/api/auth/logout`
- **Method**: POST
- **Headers**: `Authorization: Bearer {token}`

### 用户接口

#### 用户注册
- **URL**: `/api/user/register`
- **Method**: POST
- **Request Body**:
```json
{
  "username": "test",
  "password": "123456",
  "realName": "测试用户",
  "phone": "13800138000",
  "email": "test@example.com",
  "role": "USER"
}
```

#### 获取当前用户信息
- **URL**: `/api/user/info`
- **Method**: GET
- **Headers**: `Authorization: Bearer {token}`

### 管理员接口

#### 获取当前管理员信息
- **URL**: `/api/admin/info`
- **Method**: GET
- **Headers**: `Authorization: Bearer {token}`

### 居民接口

#### 创建居民
- **URL**: `/api/resident/create`
- **Method**: POST
- **Headers**: `Authorization: Bearer {token}`

#### 获取当前居民信息
- **URL**: `/api/resident/info`
- **Method**: GET
- **Headers**: `Authorization: Bearer {token}`

### 户籍接口

#### 创建户籍
- **URL**: `/api/household/create`
- **Method**: POST
- **Headers**: `Authorization: Bearer {token}`

#### 查询户籍列表
- **URL**: `/api/household/list?current=1&size=10`
- **Method**: GET
- **Headers**: `Authorization: Bearer {token}`

## 默认账号

- **用户名**: admin
- **密码**: admin123
- **角色**: ADMIN

## 注意事项

1. 所有需要认证的接口都需要在请求头中携带Token：`Authorization: Bearer {token}`
2. Token有效期为24小时
3. 密码使用MD5加密存储
4. 所有删除操作均为逻辑删除
5. 分页查询默认每页10条记录

## 开发说明

- 使用MyBatis-Plus的BaseMapper和IService，简化CRUD操作
- 使用JWT进行无状态认证
- 使用Redis存储Token，支持单点登录控制
- 统一异常处理和响应格式
- 支持逻辑删除和分页查询

## 许可证

MIT License

