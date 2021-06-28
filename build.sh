#!/usr/bin/env bash
# --------------------------------------------
# main bash build script for CI, dev and releasing. make calls this too.
# --------------------------------------------
set -e
# if build/bin does not exists then clone the bin scripts
[ ! -e build/bin ] && git clone https://github.com/yakworks/bin.git build/bin  -b 2.0.x # --single-branch --depth 1
# user.env overrides for local dev, not to be checked in
[[ -f user.env ]] && source user.env
# all.sh consolidates most of the helpful scripts from bin
source build/bin/all.sh

# default init from yml file
init_from_build_yml "gradle/build.yml"
# echo "PROJECT_NAME $PROJECT_NAME"

GRADLE_PROJECTS="gorm-tools gorm-tools-rest gorm-tools-security rally-domain examples/restify examples/testify"
# cats key files into a cache-checksum.tmp file for circle to use as key
# change this based on how project is structured
function catKeyFiles {
  cat gradle.properties build.gradle gorm-tools/build.gradle gorm-tools-rest/build.gradle gorm-tools-security/build.gradle rally-domain/build.gradle examples/restify/build.gradle examples/testify/build.gradle > cache-checksum.tmp
}

# compile used for circle
function compile {
  # Downloads Dependencies
  ./gradlew resolveConfigurations --no-daemon
  ./gradlew classes --no-daemon
#  ./gradlew testClasses
#  ./gradlew integrationTestClasses
}

# check used for circle
function check {
  #./gradlew check --max-workers=2
  ./gradlew check
  copyTestResults
}

function copyTestResults {
  for proj in $GRADLE_PROJECTS; do
    mkdir -p build/test-results/$proj
    mkdir -p build/test-reports/$proj
    cp -r $proj/build/test-results/ build/test-results/$proj
    cp -r $proj/build/reports/ build/test-reports/$proj
  done
}

# helper/debug function ex: `build.sh logVars test sqlserver`
function logVars {
  # initEnv ${1:-dev} ${2:-mysql}
  for varName in $BUILD_VARS; do
    echo "$varName = ${!varName}"
  done
}

# --- boiler plate function runner, stay at end of file ------
if declare -f "$1" > /dev/null; then
  "$@" #call function with arguments verbatim
else
  [ "$1" ] && echo "'$1' is not a known function name" >&2 && exit 1
fi
