@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup script, version 3.3.2 (Windows)
@REM See mvnw (the Unix equivalent) for what this does and why it exists.
@REM ----------------------------------------------------------------------------
@echo off
setlocal enabledelayedexpansion

set BASE_DIR=%~dp0
REM %~dp0 always ends with a trailing backslash. Left in place, that trailing
REM backslash immediately before a closing quote later in this script (e.g.
REM "-Dmaven.multiModuleProjectDirectory=%BASE_DIR%") gets misread by Windows'
REM argument parser as an escaped quote character rather than the closing quote
REM — which silently corrupts the rest of the command line and is exactly what
REM produced the generic "java usage" output instead of a real error. Stripping
REM it here is the fix.
if "%BASE_DIR:~-1%"=="\" set BASE_DIR=%BASE_DIR:~0,-1%

set WRAPPER_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPERTIES=%BASE_DIR%\.mvn\wrapper\maven-wrapper.properties

if not exist "%WRAPPER_JAR%" (
  echo Downloading Maven Wrapper...
  for /f "tokens=2 delims==" %%A in ('findstr "^wrapperUrl=" "%WRAPPER_PROPERTIES%"') do set WRAPPER_URL=%%A
  REM Delayed expansion (!VAR!) is required here — %VAR% would resolve to the
  REM value WRAPPER_URL had *before* this block started (empty), not the value
  REM the for /f loop just set, because %...% inside a ( ) block is substituted
  REM once at parse time, not per line at runtime.
  powershell -Command "Invoke-WebRequest -Uri '!WRAPPER_URL!' -OutFile '!WRAPPER_JAR!'"
)

REM Fail loudly if the jar is missing or clearly too small to be real (a failed/
REM partial download) — silently proceeding here is exactly what produced a
REM confusing generic "java usage" dump instead of a real error message.
if not exist "%WRAPPER_JAR%" (
  echo ERROR: Maven Wrapper jar was not downloaded to %WRAPPER_JAR% >&2
  exit /b 1
)
for %%F in ("%WRAPPER_JAR%") do set WRAPPER_JAR_SIZE=%%~zF
if %WRAPPER_JAR_SIZE% LSS 10000 (
  echo ERROR: Maven Wrapper jar at %WRAPPER_JAR% looks incomplete ^(size: %WRAPPER_JAR_SIZE% bytes^). Delete it and re-run. >&2
  exit /b 1
)

if "%JAVA_HOME%"=="" (
  set JAVA_CMD=java
) else (
  set JAVA_CMD=%JAVA_HOME%\bin\java
)

"%JAVA_CMD%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%BASE_DIR%" org.apache.maven.wrapper.MavenWrapperMain %*
