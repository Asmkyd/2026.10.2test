@echo off
chcp 936 >nul
setlocal

rem JDK path on this machine. Change this line if your JDK is installed elsewhere.
set "JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202"

if not exist "%JAVA_HOME%\bin\javac.exe" goto no_jdk

echo [1/2] Compiling VerificationCode.java ...
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "%~dp0." "%~dp0VerificationCode.java"
if errorlevel 1 goto compile_failed

echo [2/2] Running ...
echo.
"%JAVA_HOME%\bin\java.exe" -cp "%~dp0." VerificationCode
goto end

:no_jdk
echo [ERROR] javac.exe not found. Please check JAVA_HOME:
echo         %JAVA_HOME%
goto end

:compile_failed
echo [ERROR] Compile failed.
goto end

:end
echo.
pause
endlocal
