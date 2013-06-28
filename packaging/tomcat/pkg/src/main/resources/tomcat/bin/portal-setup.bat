@echo off
rem -------------------------------------------------------------------------
rem  GateIn Portal Setup
rem -------------------------------------------------------------------------
rem

rem $Id$

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

pushd %DIRNAME%..

set DIRNAME=

if "%OS%" == "Windows_NT" (
  set "PROGNAME=%~nx0%"
) else (
  set "PROGNAME=jdr.bat"
)

rem Setup JBoss specific properties
if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Using java.exe from PATH environment variable.
  echo Set JAVA_HOME environment variable to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)

"%JAVA%" ^
    -cp "lib\*" ^
    org.gatein.portal.installer.PortalSetupCommand ^
    -f "gatein\conf\configuration.properties" ^
     %*

:END
if "x%NOPAUSE%" == "x" pause
