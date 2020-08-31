#!/usr/bin/env bash

set -e
./gradlew benchmarks:assemble
cd examples/benchmarks
java -server -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
  -Xmx2048m -XX:MaxMetaspaceSize=256m -jar -DmultiplyData=3 -Dgpars.poolsize=4 build/libs/benchmarks.war
