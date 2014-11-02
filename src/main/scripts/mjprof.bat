@echo off 
java -Dmacros.configFile=%~dp0/macros.properties -cp "%JAVA_HOME%/lib/tools.jar;%~dp0/mjprof-1.0.jar;%~dp0/lib/*.jar;%~dp0/plugins/*" com.performizeit.mjprof.MJProf   %*
