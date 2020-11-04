#!/usr/bin/env bash

set -e
./gradlew benchmarks:assemble
cd examples/benchmarks
#-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
java -server \
  -Xmx3048m -XX:MaxMetaspaceSize=256m -jar -DmultiplyData=3 -Dgpars.poolsize=4 build/libs/benchmarks.war
