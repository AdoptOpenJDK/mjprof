@echo off 
java -cp "%JAVA_HOME%/lib/tools.jar;%~dp0/mjprof-1.0.jar;%~dp0/lib/*.jar;%~dp0/plugins/*" com.performizeit.mjstack.MJStack   %*
