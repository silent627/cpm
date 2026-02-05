@echo off
chcp 65001 >nul
echo ========================================
echo ES 同步问题诊断脚本
echo ========================================
echo.

echo [1] 检查 Elasticsearch 连接状态...
curl -s http://localhost:9200 >nul 2>&1
if %errorlevel% == 0 (
    echo ✓ Elasticsearch 连接正常
    curl -s http://localhost:9200/_cluster/health?pretty
) else (
    echo ✗ Elasticsearch 连接失败，请检查 ES 是否运行
)
echo.

echo [2] 检查 RabbitMQ 连接状态...
netstat -an | findstr ":5672" >nul 2>&1
if %errorlevel% == 0 (
    echo ✓ RabbitMQ 端口 5672 正在监听
) else (
    echo ✗ RabbitMQ 端口 5672 未监听，请检查 RabbitMQ 是否运行
)
echo.

echo [3] 检查 ES 中的用户索引...
curl -s http://localhost:9200/user_index/_search?q=id:1&pretty
echo.

echo [4] 检查用户索引的文档总数...
curl -s http://localhost:9200/user_index/_count?pretty
echo.

echo ========================================
echo 诊断完成
echo ========================================
pause
