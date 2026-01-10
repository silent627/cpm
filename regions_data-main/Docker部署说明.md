# Regions Data 服务 Docker 部署说明

## 📋 概述

Regions Data 是一个基于 FastAPI 的行政区划数据查询服务，提供省级、地级、县级、乡镇级四级行政区划数据查询接口。

## 🏗️ 服务架构

- **框架**: FastAPI
- **数据库**: SQLite (regions.db)
- **端口**: 8000
- **数据来源**: 国家地名信息库

## 🚀 Docker 部署

### 1. 构建镜像

```bash
# 构建 regions-data 镜像
docker-compose build regions-data

# 或者构建所有服务
docker-compose build
```

### 2. 启动服务

```bash
# 启动 regions-data 服务
docker-compose up -d regions-data

# 或者启动所有服务
docker-compose up -d
```

### 3. 查看服务状态

```bash
# 查看服务状态
docker-compose ps regions-data

# 查看服务日志
docker-compose logs -f regions-data
```

### 4. 访问服务

- **API 文档**: http://localhost:8000/docs
- **首页**: http://localhost:8000/
- **API 接口**: http://localhost:8000/api/provinces

## 📁 文件结构

```
regions_data-main/
├── Dockerfile              # Docker 镜像构建文件
├── main.py                 # 应用入口文件
├── requirements.txt        # Python 依赖
├── regions.db             # SQLite 数据库文件
├── api/
│   └── index.py           # FastAPI 应用
├── templates/
│   └── index.html         # 前端页面
└── data/
    └── regions_*.json     # 原始 JSON 数据
```

## ⚙️ 配置说明

### Docker Compose 配置

```yaml
regions-data:
  build:
    context: ./regions_data-main
    dockerfile: Dockerfile
  container_name: cpm-regions-data
  ports:
    - "8000:8000"
  environment:
    HOST: "0.0.0.0"      # 监听所有网络接口
    PORT: "8000"         # 服务端口
    RELOAD: "false"      # 生产环境禁用热重载
  volumes:
    - ./regions_data-main/regions.db:/app/regions.db
  networks:
    - cpm-network
```

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| HOST | 0.0.0.0 | 服务监听地址 |
| PORT | 8000 | 服务端口 |
| RELOAD | false | 是否启用热重载（开发环境可设为 true） |

## 🔧 常用命令

### 查看日志

```bash
# 查看实时日志
docker-compose logs -f regions-data

# 查看最近 50 行日志
docker-compose logs --tail=50 regions-data
```

### 重启服务

```bash
# 重启服务
docker-compose restart regions-data

# 重新构建并启动
docker-compose up -d --build regions-data
```

### 进入容器

```bash
# 进入容器
docker-compose exec regions-data bash

# 检查数据库文件
docker-compose exec regions-data ls -lh /app/regions.db
```

### 测试 API

```bash
# 测试健康检查
curl http://localhost:8000/api/provinces

# 测试统计接口
curl http://localhost:8000/api/stats

# 测试子级查询
curl http://localhost:8000/api/children/110000
```

## 📊 API 接口说明

### 1. 获取所有省份

```http
GET /api/provinces
```

**响应示例**:
```json
[
  {
    "code": "110000",
    "name": "北京市",
    "type": "直辖市",
    "type_code": "1"
  },
  ...
]
```

### 2. 获取子级区划

```http
GET /api/children/{parent_code}
```

**参数**:
- `parent_code`: 父级区划代码

**响应示例**:
```json
{
  "items": [
    {
      "code": "110100",
      "name": "市辖区",
      "level": "地级",
      "depth": 2,
      "type": "地级市",
      "type_code": "2"
    }
  ],
  "hasChildren": true,
  "childrenTypeName": "地级市",
  "hasGrandchildren": true,
  "count": 1
}
```

### 3. 获取统计信息

```http
GET /api/stats
```

**响应示例**:
```json
{
  "total": 42176,
  "by_level": [
    {
      "level": "省级",
      "count": 34
    },
    {
      "level": "地级",
      "count": 333
    },
    ...
  ]
}
```

## 🐛 故障排查

### 问题 1: 服务无法访问

**原因**: 服务可能未正确启动

**解决**:
```bash
# 检查服务状态
docker-compose ps regions-data

# 查看日志
docker-compose logs regions-data

# 重启服务
docker-compose restart regions-data
```

### 问题 2: 数据库文件不存在

**原因**: regions.db 文件未挂载或不存在

**解决**:
```bash
# 检查文件是否存在
ls -lh regions_data-main/regions.db

# 检查挂载配置
docker-compose exec regions-data ls -lh /app/regions.db
```

### 问题 3: 端口被占用

**原因**: 8000 端口已被其他服务占用

**解决**:
```bash
# 检查端口占用
netstat -ano | findstr :8000

# 修改 docker-compose.yml 中的端口映射
ports:
  - "8001:8000"  # 改为其他端口
```

### 问题 4: 健康检查失败

**原因**: 服务启动时间较长或 API 接口异常

**解决**:
```bash
# 手动测试健康检查
curl http://localhost:8000/api/provinces

# 查看服务日志
docker-compose logs regions-data

# 增加健康检查重试次数（在 docker-compose.yml 中）
healthcheck:
  retries: 10  # 增加重试次数
```

## 🔄 更新数据库

如果需要更新行政区划数据：

```bash
# 1. 停止服务
docker-compose stop regions-data

# 2. 替换 regions.db 文件
# 将新的 regions.db 文件复制到 regions_data-main/ 目录

# 3. 重启服务
docker-compose start regions-data
```

## 📝 注意事项

1. **数据持久化**: regions.db 文件通过 volumes 挂载，数据会持久化保存
2. **网络访问**: 服务监听 0.0.0.0，可以从容器外部访问
3. **性能**: SQLite 数据库适合中小规模数据查询，如需高性能可考虑迁移到 PostgreSQL
4. **数据更新**: 数据来源于国家地名信息库，建议定期更新

## 📚 相关文档

- [FastAPI 文档](https://fastapi.tiangolo.com/)
- [国家地名信息库](https://dmfw.mca.gov.cn)
- [Docker 部署指南](../Docker部署指南.md)
