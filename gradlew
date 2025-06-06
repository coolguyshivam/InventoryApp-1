#!/bin/sh
################################################################################
## Gradle start up script for UN*X                                            ##
################################################################################
DEFAULT_JVM_OPTS=""
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
CLASSPATH=$APP_BASE_NAME
exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"