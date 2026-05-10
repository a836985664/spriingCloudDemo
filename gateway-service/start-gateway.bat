@echo off
chcp 65001 >nul
echo ========================================
echo  启动 Gateway Service
echo ========================================
echo.

REM 检查 Java 环境变量
if not defined JAVA_HOME (
    echo [错误] 未设置 JAVA_HOME 环境变量
    echo 请先配置 Java 17 或更高版本
    pause
    exit /b 1
)

echo [1/3] 启动 Nacos (如果未运行)...
start /B cmd /c "java -jar D:\ideaProject\spriingCloudDemo\nacos-server-2.3.2\nacos-server.jar -Dnacos.standalone=true -Dnacos.core.context.config.namespace=public"

echo [2/3] 等待 Nacos 启动...
timeout /t 10 /nobreak >nul

echo [3/3] 启动 Gateway Service...
cd /d "%~dp0"
mvn spring-boot:run -Dspring-boot.run.profiles=dev

pause
