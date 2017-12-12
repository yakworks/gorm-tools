#!/usr/bin/env bash

set -e

echo "##### Running benchamarks simple single thread"
./gradlew assemble -DauditTrail.enabled=false
java -server -jar -DloadIterations=1 -DpoolSize=1 -DauditTrailEnabled=true -DeventListenerCount=1 -DeventSubscriberCount=0 build/libs/benchmarks.war

./gradlew assemble -DauditTrailEnabled=false; java -server -jar -DloadIterations=3 -Dgpars.poolsize=1 -DauditTrailEnabled=false build/libs/benchmarks.war

echo "##### Running benchamarks with AuditTrail"
./gradlew clean assemble -DauditTrail.enabled=true
java -jar build/libs/grails-gorm-benchmarks-0.1.war

echo "##### Running benchamarks without AuditTrail"
#gradle clean assemble --no-daemon check --stacktrace
#java -jar build/libs/grails-gorm-benchmarks-0.1.war

echo "###### Running benchamarks with custom IdGenerator"
#java -Didgenerator.enabled=true -jar build/libs/grails-gorm-benchmarks-0.1.war

echo "###### Running benchamarks with autowire off"
#java -Dautowire.enabled=false -jar build/libs/grails-gorm-benchmarks-0.1.war
