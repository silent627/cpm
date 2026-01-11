# Sentinel Dashboard 流量控制服务

## 版本信息
- **Sentinel Dashboard 版本**: 1.8.6
- **端口**: 8858
- **访问地址**: http://localhost:8858
- **默认账号**: sentinel / sentinel

## 启动方式

### 方式一：使用启动脚本（推荐）

#### 前台运行（显示日志）
双击运行 `start-sentinel.bat`

#### 后台运行（最小化窗口）
双击运行 `start-sentinel-background.bat`

### 方式二：命令行直接运行

```bash
cd tools\sentinel
java -Dserver.port=8858 -Dcsp.sentinel.dashboard.server=localhost:8858 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar
```

### 方式三：自定义配置运行

```bash
# 自定义端口
java -Dserver.port=8859 -jar sentinel-dashboard.jar

# 自定义账号密码（需要修改源码重新编译）
# 默认账号: sentinel / sentinel
```

## 停止服务

### 前台运行
按 `Ctrl + C` 停止

### 后台运行
1. 打开任务管理器（Ctrl + Shift + Esc）
2. 找到 "Sentinel Dashboard" 进程
3. 结束进程

或使用命令行：
```bash
taskkill /FI "WINDOWTITLE eq Sentinel Dashboard*" /F
```

## 环境要求

- **Java 版本**: JDK 1.8 或更高版本
- **内存**: 建议至少 512MB 可用内存

## 验证服务

启动后，在浏览器访问：http://localhost:8858

使用默认账号登录：
- **用户名**: sentinel
- **密码**: sentinel

如果看到 Sentinel Dashboard 的管理界面，说明服务启动成功。

## 与项目集成

项目中的微服务已配置 Sentinel 流量控制，启动 Sentinel Dashboard 后，微服务会自动连接到 Dashboard。

配置位置：各服务的 `application.yml` 中
```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: http://localhost:8858
        port: 8719
```

## 注意事项

1. 确保端口 8858 未被占用
2. 如果使用 Docker 方式运行 Sentinel，请先停止 Docker 容器，避免端口冲突
3. 默认使用内存存储，服务重启后规则会丢失
4. 生产环境建议配置持久化存储（如 Nacos、Apollo 等）

## 功能说明

Sentinel Dashboard 提供以下功能：
- **实时监控**: 查看各服务的实时 QPS、响应时间等指标
- **流控规则**: 配置限流规则，保护服务不被流量压垮
- **降级规则**: 配置熔断降级规则，提高系统稳定性
- **热点规则**: 配置热点参数限流
- **系统规则**: 配置系统自适应限流
- **授权规则**: 配置黑白名单

## 与 Zipkin 的区别

- **Zipkin**: 分布式追踪，查看请求链路和调用关系
- **Sentinel**: 流量控制，限流、熔断、降级保护

两者功能不同，可以同时运行，共同保障微服务系统的稳定性和可观测性。
