#!/usr/bin/env bash

set -e
./gradlew assemble; java -server -jar -DmultiplyData=3 -Dgpars.poolsize=5 build/libs/benchmarks.war

# echo "###### Running benchmarks with Second Level cache"
#gradle assemble; java -server -jar -DmultiplyData=3 -Dgpars.poolsize=1 -DsecondLevelCache=true build/libs/benchmarks.war

