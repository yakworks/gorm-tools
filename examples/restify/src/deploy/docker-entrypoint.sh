#!/bin/bash

set -e

source /app/bin/utils.sh
echo "$0: Starting RCM-API"

# bash cheat sheet https://devhints.io/bash
# sets default values
# : ${EXTRA_JAVA_OPTS:=-noverify -XX:TieredStopAtLevel=1}
: ${EXTRA_JAVA_OPTS:=-Xmx3048m -XX:MaxMetaspaceSize=256m}
: ${JAVA_OPTS:=-server -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap}
: ${GRAILS_ENV:=prod}

appProps=`transform_to_java_props "$APP_PROPS"`
echo "transform appProps $appProps"

# takes Start-Class: foo.Application and just returns `foo.Application` but MANIFEST is tricky to read
# `cut -d' ' -f2` splits string on space and sed 's/.$//' removes the strange EOL char
[ ! "$START_CLASS" ] && START_CLASS=$(cat /app/jar/META-INF/MANIFEST.MF | grep "Start-Class" | cut -d':' -f2 | sed 's/.$//')

# exploded JAR
execApp="exec java $JAVA_OPTS $EXTRA_JAVA_OPTS \
  -Dgrails.env=$GRAILS_ENV -Djava.awt.headless=true -Dlogging.config=/app/logback.groovy \
  $appProps \
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
