# Docker 快速启动指南

## 🚀 一键启动

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

## 📍 服务访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| API网关 | http://localhost:8080 | 统一入口 |
| API文档 | http://localhost:8080/doc.html | Swagger文档 |
| Nacos | http://localhost:8848/nacos | 服务注册中心 (nacos/nacos) |
| RabbitMQ | http://localhost:15672 | 消息队列 (guest/guest) |
| Regions API | http://localhost:8000/docs | 行政区划API |

## 🔧 常用命令

```bash
# 停止所有服务
docker-compose down

# 重启服务
docker-compose restart

# 查看指定服务日志
docker-compose logs -f user-service

# 进入容器
docker-compose exec mysql bash
```

## ⚠️ 注意事项

1. **首次启动**：需要等待数据库初始化完成（约1-2分钟）
2. **内存要求**：至少4GB可用内存
3. **端口占用**：确保以下端口未被占用：
   - 3306 (MySQL)
   - 6379 (Redis)
   - 5672, 15672 (RabbitMQ)
   - 8848, 9848 (Nacos)
   - 8080-8085 (微服务)
   - 8000 (Regions Data)

## 🐛 故障排查

```bash
# 查看所有服务状态
docker-compose ps

# 查看服务日志
docker-compose logs service-name

# 重启失败的服务
docker-compose restart service-name
```

详细文档请参考：[Docker部署指南.md](./Docker部署指南.md)
