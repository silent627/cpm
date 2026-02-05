@echo off
chcp 65001 >nul
echo ========================================
echo    Elasticsearch 搜索服务启动脚本
echo ========================================
echo.

rem 获取脚本所在目录
cd /d %~dp0

rem 设置 ES_JAVA_HOME 指向 Elasticsearch 自带的 JDK
set ES_JAVA_HOME=%~dp0jdk

rem 临时取消 JAVA_HOME，避免使用系统的 JDK 8
set JAVA_HOME=

echo 使用 Elasticsearch 自带的 JDK: %ES_JAVA_HOME%
echo.

rem 切换到 bin 目录
cd /d %~dp0bin

echo 正在启动 Elasticsearch...
echo 访问地址: http://localhost:9200
echo 按 Ctrl+C 停止服务
echo.

rem 启动 Elasticsearch
call elasticsearch.bat

pause
