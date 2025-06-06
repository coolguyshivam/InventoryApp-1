#!/bin/sh
export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home)}
exec "$JAVA_HOME/bin/java" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
