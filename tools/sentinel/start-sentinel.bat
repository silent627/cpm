@echo off
chcp 65001 >nul
echo ========================================
echo    Sentinel Dashboard 流量控制服务启动脚本
echo ========================================
echo.

cd /d %~dp0

echo 正在启动 Sentinel Dashboard...
echo 访问地址: http://localhost:8858
echo 默认账号: sentinel / sentinel
echo 按 Ctrl+C 停止服务
echo.

java -Dserver.port=8858 -Dcsp.sentinel.dashboard.server=localhost:8858 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar

pause
