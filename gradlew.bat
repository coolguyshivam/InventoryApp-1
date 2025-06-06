@echo off
setlocal
set JAVA_HOME=%JAVA_HOME%
"%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
