#!/usr/bin/env bash

set -e
./gradlew assemble; java -server -jar -DmultiplyData=3 -Dgorm.tools.async.poolSize=5 build/libs/benchmarks.war

# echo "###### Running benchmarks with Second Level cache"
#gradle assemble; java -server -jar -DmultiplyData=3 -Dgorm.tools.async.poolSize=1 -DsecondLevelCache=true build/libs/benchmarks.war

