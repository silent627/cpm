@echo off
chcp 65001 >nul
echo ========================================
echo    Zipkin 分布式追踪服务（后台启动）
echo ========================================
echo.

cd /d %~dp0

echo 正在后台启动 Zipkin Server...
start "Zipkin Server" /min java -jar zipkin-server.jar

timeout /t 3 >nul
echo Zipkin Server 已在后台启动
echo 访问地址: http://localhost:9411
echo 查看日志请打开任务管理器，找到 "Zipkin Server" 窗口
echo.

pause
