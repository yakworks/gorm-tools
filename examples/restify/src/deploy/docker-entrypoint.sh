#!/bin/bash

set -e

source /app/bin/utils.sh
echo "$0: Starting RCM-API"

# bash cheat sheet https://devhints.io/bash
# sets default values
# : ${EXTRA_JAVA_OPTS:=-noverify -XX:TieredStopAtLevel=1}
: ${JAVA_OPTS:=-Xmx3g -XX:MaxMetaspaceSize=256m}
SERVER_JAVA_OPTS="-server
-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
GC_JAVA_OPTS="-XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly
-XX:+CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=70
-XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark"

: ${GRAILS_ENV:=prod}

# TODO need to work out best defualt jvm settings
# https://www.maknesium.de/21-most-important-java-8-vm-options-for-servers
# https://www.bswen.com/2019/05/springboot-Ways-to-do-JVM-optimizations-for-springboot-apps.html
# https://blog.sokolenko.me/2014/11/javavm-options-production.html
# https://www.javacodegeeks.com/2017/11/minimize-java-memory-usage-right-garbage-collector.html
#  -XX:MetaspaceSize=128m
#  -XX:MaxMetaspaceSize=128m
#  -Xms1024m -Xmx1024m -Xmn256m
#  -Xss256k -XX:SurvivorRatio=8
#  -XX:+UseConcMarkSweepGC

echo "APP_PROPS: $APP_PROPS"
APP_PROPS_JAVA=`transform_to_java_props "$APP_PROPS"`
echo "transformed APP_PROPS to $APP_PROPS_JAVA"

# takes Start-Class: foo.Application and just returns `foo.Application` but MANIFEST is tricky to read
# `cut -d' ' -f2` splits string on space and sed 's/.$//' removes the strange EOL char
[ ! "$START_CLASS" ] && START_CLASS=$(cat /app/jar/META-INF/MANIFEST.MF | grep "Start-Class" | cut -d':' -f2 | sed 's/.$//')

# exploded JAR
execApp="exec java $SERVER_JAVA_OPTS
$GC_JAVA_OPTS
$JAVA_OPTS
$APP_PROPS_JAVA
-Dgrails.env=$GRAILS_ENV -Djava.awt.headless=true
-cp /app/jar:/app/jar/lib/* ${START_CLASS}"

# java -cp /app/jar:/app/jar/lib/* restify.Application
# single jar
# execApp="exec java $JAVA_OPTS $EXTRA_JAVA_OPTIONS -Dgrails.env=$GRAILS_ENV -Djava.awt.headless=true  \
# $appProps \
# -jar /app/jar/app.jar"

echo "running:"
echo "$execApp"

#run it
eval $execApp
