#!/usr/bin/env bash

set -e
gradle assemble; java -server -jar -DmultiplyData=3 -Dgpars.poolsize=1 build/libs/benchmarks.war

# echo "###### Running benchmarks with Second Level cache"
#gradle assemble; java -server -jar -DmultiplyData=3 -Dgpars.poolsize=1 -DsecondLevelCache=true build/libs/benchmarks.war

