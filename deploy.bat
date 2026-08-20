@echo off
setlocal

echo ==========================================
echo        ORBIT STORE - DEPLOYMENT
echo ==========================================
echo.

REM ==========================================
REM CONFIGURATION
REM ==========================================

set "PROJECT_DIR=D:\CHARAN_MAHENDARAN\PROJECTS\ORBIT-STORE"
set "BACKEND_DIR=%PROJECT_DIR%\backend"

set "TOMCAT_HOME=C:\Program Files\Apache Software Foundation\apache-tomcat-10.1.57"
set "CATALINA_HOME=%TOMCAT_HOME%"
set "CATALINA_BASE=%TOMCAT_HOME%"
set "WEBAPPS=%TOMCAT_HOME%\webapps"

set "WAR_NAME=orbit-store-backend.war"
set "WAR_FILE=%BACKEND_DIR%\target\%WAR_NAME%"

REM ==========================================
REM CHECK TOMCAT
REM ==========================================

echo [1/5] Checking Tomcat...

if not exist "%TOMCAT_HOME%" (
    echo ERROR: Tomcat directory not found.
    echo.
    echo Expected:
    echo %TOMCAT_HOME%
    echo.
    pause
    exit /b 1
)

echo Tomcat found.
echo.

REM ==========================================
REM STOP TOMCAT
REM ==========================================

echo [2/5] Stopping Tomcat...

call "%TOMCAT_HOME%\bin\shutdown.bat"

echo Waiting for Tomcat to stop...
timeout /t 5 /nobreak >nul

echo Tomcat stop command completed.
echo.

REM ==========================================
REM BUILD BACKEND
REM ==========================================

echo [3/5] Building backend...

cd /d "%BACKEND_DIR%"

call mvn clean package

if errorlevel 1 (
    echo.
    echo ERROR: Maven build failed.
    echo Deployment cancelled.
    echo.
    pause
    exit /b 1
)

echo.
echo Maven build successful.
echo.

REM ==========================================
REM REMOVE OLD DEPLOYMENT
REM ==========================================

echo [4/5] Removing previous deployment...

if exist "%WEBAPPS%\%WAR_NAME%" (
    del /f /q "%WEBAPPS%\%WAR_NAME%"
    echo Old WAR removed.
)

if exist "%WEBAPPS%\orbit-store-backend" (
    rmdir /s /q "%WEBAPPS%\orbit-store-backend"
    echo Old extracted application removed.
)

echo.

REM ==========================================
REM COPY NEW WAR
REM ==========================================

if not exist "%WAR_FILE%" (
    echo ERROR: New WAR file was not found.
    echo Expected:
    echo %WAR_FILE%
    echo.
    pause
    exit /b 1
)

echo Copying new WAR...

copy /y "%WAR_FILE%" "%WEBAPPS%\%WAR_NAME%"

if errorlevel 1 (
    echo.
    echo ERROR: Failed to copy WAR.
    echo.
    pause
    exit /b 1
)

echo WAR copied successfully.
echo.

REM ==========================================
REM START TOMCAT
REM ==========================================

echo [5/5] Starting Tomcat...

call "%TOMCAT_HOME%\bin\startup.bat"

if errorlevel 1 (
    echo.
    echo ERROR: Tomcat failed to start.
    echo Deployment was copied, but Tomcat is not running.
    echo.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo       DEPLOYMENT COMPLETED
echo ==========================================
echo.
echo WAR:
echo %WAR_FILE%
echo.
echo Tomcat:
echo %TOMCAT_HOME%
echo.
echo Application:
echo http://localhost:8080/orbit-store-backend/
echo.
echo ==========================================

pause
endlocal