@echo off
setlocal
set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MVN=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"
if not exist "%MVN%" (
  echo Maven was not found. Open this folder in IntelliJ IDEA and run ImmuneCareApplication.
  exit /b 1
)
call "%MVN%" spring-boot:run
endlocal
