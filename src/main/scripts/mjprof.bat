@echo off 
java -Dmacros.configFile=%~dp0/macros.properties -cp "%JAVA_HOME%/lib/tools.jar;%~dp0/mjprof-1.0-jar-with-dependencies.jar;%~dp0/plugins/*" com.performizeit.mjprof.MJProf   %*
