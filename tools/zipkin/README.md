# Zipkin 分布式追踪服务

## 版本信息
- **Zipkin Server 版本**: 2.24.1
- **端口**: 9411
- **访问地址**: http://localhost:9411

## 启动方式

### 方式一：使用启动脚本（推荐）

#### 前台运行（显示日志）
双击运行 `start-zipkin.bat`

#### 后台运行（最小化窗口）
双击运行 `start-zipkin-background.bat`

### 方式二：命令行直接运行

```bash
cd tools\zipkin
java -jar zipkin-server.jar
```

### 方式三：指定配置运行

```bash
# 使用内存存储（默认）
java -jar zipkin-server.jar --STORAGE_TYPE=mem

# 使用 Elasticsearch 存储
java -jar zipkin-server.jar --STORAGE_TYPE=elasticsearch --ES_HOSTS=http://localhost:9200

# 使用 MySQL 存储
java -jar zipkin-server.jar --STORAGE_TYPE=mysql --MYSQL_HOST=localhost --MYSQL_TCP_PORT=3306 --MYSQL_DB=zipkin --MYSQL_USER=root --MYSQL_PASS=123456
```

## 停止服务

### 前台运行
按 `Ctrl + C` 停止

### 后台运行
1. 打开任务管理器（Ctrl + Shift + Esc）
2. 找到 "Zipkin Server" 进程
3. 结束进程

或使用命令行：
```bash
taskkill /FI "WINDOWTITLE eq Zipkin Server*" /F
```

## 环境要求

- **Java 版本**: JDK 1.8 或更高版本
- **内存**: 建议至少 512MB 可用内存

## 验证服务

启动后，在浏览器访问：http://localhost:9411

如果看到 Zipkin 的 Web 界面，说明服务启动成功。

## 与项目集成

项目中的微服务已配置 Zipkin 追踪，启动 Zipkin Server 后，微服务的追踪数据会自动发送到 Zipkin。

配置位置：各服务的 `application.yml` 中
```yaml
spring:
  sleuth:
    zipkin:
      base-url: http://localhost:9411
```

## 注意事项

1. 确保端口 9411 未被占用
2. 如果使用 Docker 方式运行 Zipkin，请先停止 Docker 容器，避免端口冲突
3. 默认使用内存存储，服务重启后数据会丢失
4. 生产环境建议使用 Elasticsearch 或 MySQL 作为存储后端
