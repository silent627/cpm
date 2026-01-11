@echo off
chcp 65001 >nul
echo ========================================
echo    Zipkin 分布式追踪服务启动脚本
echo ========================================
echo.

cd /d %~dp0

echo 正在启动 Zipkin Server...
echo 访问地址: http://localhost:9411
echo 按 Ctrl+C 停止服务
echo.

java -jar zipkin-server.jar

pause
