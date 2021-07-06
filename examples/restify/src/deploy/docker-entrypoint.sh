#!/bin/bash

set -e

source /app/bin/utils.sh
echo "$0: Starting RCM-API"

# bash cheat sheet https://devhints.io/bash
# sets default values
: ${EXTRA_JAVA_OPTS:=-noverify -XX:TieredStopAtLevel=1}
: ${JAVA_OPTS:=-server -Xmx3048m -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxMetaspaceSize=256m}
: ${GRAILS_ENV:=prod}

appProps=`transform_to_java_props "$APP_PROPS"`

execApp="exec java $JAVA_OPTS $EXTRA_JAVA_OPTIONS \
$appProps \
-Dgrails.env=$GRAILS_ENV \
-Djava.awt.headless=true  \
-jar /app/jar/app.jar"

echo "$execApp"

#run it
eval $execApp

#java -server -Xmx3048m -XX:MaxMetaspaceSize=256m -jar /app/jar/rest-api.jar
