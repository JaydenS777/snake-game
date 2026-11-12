@echo off
setlocal
cd /d "%~dp0"

set "SNAKE_JAVA_HOME="
if exist "%ProgramFiles%\Java\jdk-25\bin\java.exe" set "SNAKE_JAVA_HOME=%ProgramFiles%\Java\jdk-25"
if not defined SNAKE_JAVA_HOME if defined JAVA_HOME set "SNAKE_JAVA_HOME=%JAVA_HOME%"

if not defined SNAKE_JAVA_HOME (
    echo Java 25 was not found. Install JDK 25 or set JAVA_HOME to a compatible JDK.
    exit /b 1
)

if not exist "%SNAKE_JAVA_HOME%\bin\java.exe" (
    echo Could not find java.exe under "%SNAKE_JAVA_HOME%".
    echo Set JAVA_HOME to your JDK installation folder and try again.
    exit /b 1
)

set "JAVA_HOME=%SNAKE_JAVA_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call "%~dp0mvnw.cmd" javafx:run
exit /b %ERRORLEVEL%
