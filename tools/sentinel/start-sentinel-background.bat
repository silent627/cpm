@echo off
chcp 65001 >nul
echo ========================================
echo    Sentinel Dashboard（后台启动）
echo ========================================
echo.

cd /d %~dp0

echo 正在后台启动 Sentinel Dashboard...
start "Sentinel Dashboard" /min java -Dserver.port=8858 -Dcsp.sentinel.dashboard.server=localhost:8858 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar

timeout /t 3 >nul
echo Sentinel Dashboard 已在后台启动
echo 访问地址: http://localhost:8858
echo 默认账号: sentinel / sentinel
echo 查看日志请打开任务管理器，找到 "Sentinel Dashboard" 窗口
echo.

pause
