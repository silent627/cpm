# CPM系统 Docker 部署指南

## 📋 目录
1. [环境要求](#环境要求)
2. [快速开始](#快速开始)
3. [服务说明](#服务说明)
4. [常用命令](#常用命令)
5. [故障排查](#故障排查)

---

## 环境要求

### 必需环境
- **Docker**: 20.10 或更高版本
- **Docker Compose**: 2.0 或更高版本
- **内存**: 至少 4GB 可用内存
- **磁盘**: 至少 10GB 可用空间

### 验证环境
```bash
# 检查Docker版本
docker --version

# 检查Docker Compose版本
docker-compose --version

# 检查Docker是否运行
docker info
```

---

## 快速开始

### 1. 启动所有服务

```bash
# 在项目根目录执行
docker-compose up -d
```

### 2. 查看服务状态

```bash
# 查看所有容器状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 访问服务

- **前端应用**: http://localhost:3000
- **API网关**: http://localhost:8080
- **API文档**: http://localhost:8080/doc.html
- **Nacos控制台**: http://localhost:8848/nacos (nacos/nacos)
- **RabbitMQ管理**: http://localhost:15672 (guest/guest)
- **Regions Data API**: http://localhost:8000/docs

---

## 服务说明

### 基础服务

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库服务 |
| Redis | 6379 | 缓存服务 |
| RabbitMQ | 5672, 15672 | 消息队列服务 |
| Elasticsearch | 9200, 9300 | 搜索引擎 |
| Nacos | 8848, 9848 | 服务注册与配置中心 |

### 业务服务

| 服务 | 端口 | 说明 |
|------|------|------|
| Frontend | 3000 | 前端应用（Vue 3 + Nginx） |
| Gateway | 8080 | API网关 |
| User Service | 8081 | 用户服务 |
| Resident Service | 8082 | 居民服务 |
| Household Service | 8083 | 户籍服务 |
| Statistics Service | 8084 | 统计服务 |
| Search Service | 8085 | 搜索服务 |
| Regions Data | 8000 | 行政区划数据服务 |

---

## 常用命令

### 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 启动指定服务
docker-compose up -d mysql redis nacos

# 启动业务服务（需要先启动基础服务）
docker-compose up -d user-service resident-service
```

### 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止指定服务
docker-compose stop user-service

# 停止并删除容器
docker-compose down -v
```

### 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看指定服务日志
docker-compose logs -f user-service

# 查看最近100行日志
docker-compose logs --tail=100 user-service
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启指定服务
docker-compose restart user-service
```

### 构建镜像

```bash
# 构建所有服务镜像
docker-compose build

# 构建指定服务镜像
docker-compose build user-service

# 构建前端镜像（需要先安装依赖）
docker-compose build frontend
```

### 前端服务说明

前端服务使用多阶段构建：
1. **构建阶段**: 使用 Node.js 18 构建 Vue 3 应用
2. **运行阶段**: 使用 Nginx Alpine 提供静态文件服务

前端服务特性：
- 自动代理 `/api` 请求到 Gateway 服务
- 自动代理 `/uploads` 请求到 Gateway 服务
- 支持 Vue Router 历史模式
- 启用 Gzip 压缩
- 静态资源缓存优化

**前端构建说明**：
```bash
# 单独构建前端镜像
docker-compose build frontend

# 重新构建前端（不使用缓存）
docker-compose build --no-cache frontend

# 查看前端构建日志
docker-compose build frontend --progress=plain
```

# 强制重新构建（不使用缓存）
docker-compose build --no-cache user-service
```

### 进入容器

```bash
# 进入MySQL容器
docker-compose exec mysql bash

# 进入Redis容器
docker-compose exec redis sh

# 进入业务服务容器
docker-compose exec user-service sh
```

---

## 数据管理

### 数据库初始化

数据库初始化脚本会自动执行（位于 `src/main/resources/sql/init.sql`）

如果需要手动执行：

```bash
# 进入MySQL容器
docker-compose exec mysql bash

# 登录MySQL
mysql -uroot -p123456

# 执行SQL脚本
source /docker-entrypoint-initdb.d/init.sql
```

### 数据备份

```bash
# 备份MySQL数据
docker-compose exec mysql mysqldump -uroot -p123456 cpm_db > backup.sql

# 备份Redis数据
docker-compose exec redis redis-cli SAVE
docker cp cpm-redis:/data/dump.rdb ./redis-backup.rdb
```

### 数据恢复

```bash
# 恢复MySQL数据
docker-compose exec -T mysql mysql -uroot -p123456 cpm_db < backup.sql

# 恢复Redis数据
docker cp ./redis-backup.rdb cpm-redis:/data/dump.rdb
docker-compose restart redis
```

---

## 配置说明

### 环境变量

各服务的环境变量可在 `docker-compose.yml` 中修改：

```yaml
user-service:
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/cpm_db
    SPRING_DATASOURCE_USERNAME: root
    SPRING_DATASOURCE_PASSWORD: 123456
    SPRING_REDIS_HOST: redis
    SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR: nacos:8848
```

### 端口映射

如需修改端口，编辑 `docker-compose.yml`：

```yaml
gateway:
  ports:
    - "8080:8080"  # 修改为 "9090:8080" 即可使用9090端口
```

### 数据持久化

所有数据存储在Docker volumes中：

```bash
# 查看volumes
docker volume ls

# 查看volume详情
docker volume inspect cpm_mysql_data

# 删除volume（谨慎操作）
docker volume rm cpm_mysql_data
```

---

## 故障排查

### 1. 服务启动失败

```bash
# 查看服务日志
docker-compose logs service-name

# 检查服务状态
docker-compose ps

# 检查容器资源使用
docker stats
```

### 2. 数据库连接失败

```bash
# 检查MySQL是否运行
docker-compose ps mysql

# 检查MySQL日志
docker-compose logs mysql

# 测试MySQL连接
docker-compose exec mysql mysql -uroot -p123456 -e "SELECT 1"
```

### 3. Nacos连接失败

```bash
# 检查Nacos是否运行
docker-compose ps nacos

# 查看Nacos日志
docker-compose logs nacos

# 访问Nacos控制台
# http://localhost:8848/nacos
```

### 4. 服务注册失败

- 检查Nacos是否正常运行
- 检查服务配置中的Nacos地址是否正确
- 查看服务日志确认错误信息

### 5. 端口冲突

```bash
# 查看端口占用
netstat -ano | findstr :8080

# 修改docker-compose.yml中的端口映射
```

### 6. 内存不足

```bash
# 查看容器资源使用
docker stats

# 限制容器内存（在docker-compose.yml中）
services:
  user-service:
    deploy:
      resources:
        limits:
          memory: 512M
```

---

## 生产环境建议

### 1. 安全配置

- 修改默认密码（MySQL、Redis、RabbitMQ）
- 启用Nacos认证
- 配置防火墙规则
- 使用HTTPS

### 2. 性能优化

- 配置JVM参数
- 调整数据库连接池
- 配置Redis缓存策略
- 启用Elasticsearch集群

### 3. 监控告警

- 配置服务健康检查
- 集成监控系统（Prometheus、Grafana）
- 配置日志收集（ELK）

### 4. 备份策略

- 定期备份数据库
- 备份配置文件
- 备份上传文件

---

## 快速命令参考

```bash
# 一键启动
docker-compose up -d

# 一键停止
docker-compose down

# 一键重启
docker-compose restart

# 查看所有日志
docker-compose logs -f

# 清理所有（包括数据）
docker-compose down -v

# 重新构建并启动
docker-compose up -d --build
```

---

## 联系与支持

如有问题，请查看：
- 服务日志：`docker-compose logs -f service-name`
- Nacos控制台：http://localhost:8848/nacos
- API文档：http://localhost:8080/doc.html

---

**祝使用愉快！** 🎉
