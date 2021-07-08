#!/bin/bash

set -e
# set -u  # Attempt to use undefined variable outputs error message, and forces an exit
set -x  # Similar to verbose mode (-v), but expands commands
set -o pipefail  # Causes a pipeline to return the exit status of the last command in the pipe that returned a non-zero return value.


source /app/bin/utils.sh
echo "$0: Starting RCM-API"
# bash cheat sheet https://devhints.io/bash

# if JAVA_OPTS env is not passed in then set defaults
[ ! "$JAVA_OPTS" ]&& JAVA_OPTS="-Xmx3g -XX:MaxMetaspaceSize=256m"
# -noverify -XX:TieredStopAtLevel=1} # good for dev
SERVER_JAVA_OPTS="-server
-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

GC_JAVA_OPTS="-XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly
-XX:+CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=70
-XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark
-XX:SurvivorRatio=8"

: ${GRAILS_ENV:=prod}

# TODO need to work out best defualt jvm settings
# https://www.maknesium.de/21-most-important-java-8-vm-options-for-servers
# https://www.bswen.com/2019/05/springboot-Ways-to-do-JVM-optimizations-for-springboot-apps.html
# https://blog.sokolenko.me/2014/11/javavm-options-production.html
# https://www.javacodegeeks.com/2017/11/minimize-java-memory-usage-right-garbage-collector.html

echo "APP_PROPS: $APP_PROPS"
APP_PROPS_JAVA=`transform_to_java_props "$APP_PROPS"`
echo "transformed APP_PROPS to $APP_PROPS_JAVA"

# pull out the Start-Class: in MANIFEST.MF that was in jar
# MANIFEST uses carriage returns so use printf \r to store one
cr_char=`printf '\r'` # carriage return
# `cut -d':' -f2` splits string on colon and sed s/$cr_char// removes the ^M carriage return and s/ // removes space
START_CLASS=`cat /app/jar/META-INF/MANIFEST.MF | grep "Start-Class" | cut -d':' -f2 |  sed "s/ //; s/$cr_char//"`

# exploded JAR
execApp="exec java $SERVER_JAVA_OPTS
$GC_JAVA_OPTS
$JAVA_OPTS
$APP_PROPS_JAVA
-Dgrails.env=$GRAILS_ENV -Djava.awt.headless=true
-cp /app/jar:/app/jar/lib/* ${START_CLASS}"

# single jar # -jar /app/jar/app.jar"

echo "running:"
echo "$execApp"

#run it
eval $execApp
