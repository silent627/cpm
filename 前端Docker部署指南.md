# 前端 Docker 部署指南

## 📋 概述

前端服务使用 Docker 多阶段构建，将 Vue 3 应用构建为生产版本，并使用 Nginx 提供静态文件服务。

## 🏗️ 架构说明

### 构建流程

1. **构建阶段** (Node.js 18 Alpine)
   - 安装依赖 (`npm install`)
   - 构建生产版本 (`npm run build`)
   - 生成 `dist/` 目录

2. **运行阶段** (Nginx Alpine)
   - 复制构建产物到 Nginx 目录
   - 配置 Nginx 反向代理
   - 提供静态文件服务

### 服务特性

- ✅ 自动代理 `/api` 请求到 Gateway 服务
- ✅ 自动代理 `/uploads` 请求到 Gateway 服务
- ✅ 支持 Vue Router 历史模式
- ✅ 启用 Gzip 压缩
- ✅ 静态资源缓存优化
- ✅ 健康检查支持

## 🚀 快速开始

### 1. 构建前端镜像

```bash
# 构建前端镜像
docker-compose build frontend

# 强制重新构建（不使用缓存）
docker-compose build --no-cache frontend
```

### 2. 启动前端服务

```bash
# 启动前端服务（会自动启动依赖的 Gateway）
docker-compose up -d frontend

# 或者启动所有服务
docker-compose up -d
```

### 3. 访问前端应用

- **前端地址**: http://localhost:3000
- **健康检查**: http://localhost:3000/health

## 📁 文件结构

```
项目根目录/
├── Dockerfile.frontend      # 前端 Dockerfile
├── nginx.conf              # Nginx 配置文件
├── docker-compose.yml      # Docker Compose 配置
└── src/main/resources/frontend/  # 前端源代码
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
```

## ⚙️ 配置说明

### Nginx 配置 (nginx.conf)

主要配置项：

1. **API 代理**
   ```nginx
   location /api {
       proxy_pass http://gateway:8080;
   }
   ```

2. **文件上传代理**
   ```nginx
   location /uploads {
       proxy_pass http://gateway:8080;
       client_max_body_size 10M;
   }
   ```

3. **Vue Router 历史模式支持**
   ```nginx
   location / {
       try_files $uri $uri/ /index.html;
   }
   ```

### Docker Compose 配置

```yaml
frontend:
  build:
    context: .
    dockerfile: Dockerfile.frontend
  container_name: cpm-frontend
  ports:
    - "3000:80"
  depends_on:
    gateway:
      condition: service_started
  networks:
    - cpm-network
```

## 🔧 常用命令

### 查看服务状态

```bash
# 查看前端服务状态
docker-compose ps frontend

# 查看所有服务状态
docker-compose ps
```

### 查看日志

```bash
# 查看前端服务日志
docker-compose logs -f frontend

# 查看最近 50 行日志
docker-compose logs --tail=50 frontend
```

### 重启服务

```bash
# 重启前端服务
docker-compose restart frontend

# 重新构建并启动
docker-compose up -d --build frontend
```

### 进入容器

```bash
# 进入前端容器
docker-compose exec frontend sh

# 检查 Nginx 配置
docker-compose exec frontend nginx -t

# 重新加载 Nginx 配置
docker-compose exec frontend nginx -s reload
```

## 🐛 故障排查

### 1. 前端页面无法访问

```bash
# 检查容器是否运行
docker-compose ps frontend

# 查看容器日志
docker-compose logs frontend

# 检查端口是否被占用
netstat -ano | findstr :3000
```

### 2. API 请求失败

```bash
# 检查 Gateway 服务是否运行
docker-compose ps gateway

# 检查网络连接
docker-compose exec frontend ping gateway

# 查看 Gateway 日志
docker-compose logs gateway
```

### 3. 构建失败

```bash
# 查看详细构建日志
docker-compose build frontend --progress=plain

# 清理构建缓存
docker-compose build --no-cache frontend

# 检查前端源代码
ls -la src/main/resources/frontend/
```

### 4. Nginx 配置错误

```bash
# 进入容器检查配置
docker-compose exec frontend nginx -t

# 查看 Nginx 错误日志
docker-compose exec frontend cat /var/log/nginx/error.log
```

## 🔄 更新前端

### 方法 1: 重新构建并启动

```bash
# 1. 修改前端代码
# 2. 重新构建镜像
docker-compose build frontend

# 3. 重启服务
docker-compose up -d frontend
```

### 方法 2: 使用缓存构建（更快）

```bash
# 只重新构建前端
docker-compose build frontend

# 重启服务
docker-compose restart frontend
```

## 📊 性能优化

### 1. 构建优化

- 使用多阶段构建，减小镜像大小
- 使用 `.dockerignore` 排除不必要的文件
- 利用 Docker 构建缓存

### 2. Nginx 优化

- 启用 Gzip 压缩
- 静态资源缓存
- 合理的超时设置

### 3. 代码分割

构建时提示：
```
Some chunks are larger than 500 kB after minification.
```

建议：
- 使用动态 `import()` 进行代码分割
- 配置 `build.rollupOptions.output.manualChunks`

## 🔒 安全建议

1. **生产环境配置**
   - 使用 HTTPS
   - 配置 CORS
   - 限制文件上传大小

2. **Nginx 安全头**
   ```nginx
   add_header X-Frame-Options "SAMEORIGIN";
   add_header X-Content-Type-Options "nosniff";
   add_header X-XSS-Protection "1; mode=block";
   ```

## 📝 注意事项

1. **端口映射**: 前端服务映射到主机的 3000 端口
2. **依赖关系**: 前端服务依赖 Gateway 服务，确保 Gateway 先启动
3. **网络**: 前端服务需要与 Gateway 在同一 Docker 网络中
4. **构建时间**: 首次构建可能需要较长时间（下载依赖）

## 🎯 验证部署

### 1. 检查服务状态

```bash
docker-compose ps frontend
```

应该显示：
```
STATUS: Up X seconds (healthy)
```

### 2. 访问健康检查

```bash
curl http://localhost:3000/health
```

应该返回：
```
healthy
```

### 3. 访问前端页面

在浏览器中打开：http://localhost:3000

应该能看到前端登录页面。

### 4. 测试 API 代理

打开浏览器开发者工具，查看网络请求，确认 `/api` 请求能正常代理到后端。

---

## 📚 相关文档

- [Docker 部署指南](./Docker部署指南.md)
- [启动指南](./启动指南.md)
- [API 文档](./API_DOCUMENTATION.md)
