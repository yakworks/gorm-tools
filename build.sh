#!/usr/bin/env bash
# --------------------------------------------
# main build script for releasing and helpers
# --------------------------------------------
# if build/bin does not exists then clone the bin scripts
[ ! -e build/bin ] && git clone https://github.com/yakworks/bin.git build/bin --single-branch --depth 1;
# user.env overrides for local dev, not to be checked in
[[ -f user.env ]] && source user.env
set -e
# import build_functions.sh which has the the other source imports for functions
source build/bin/build_functions.sh

# create variables from the yaml file
create_yml_variables gradle/build.yml
[ "$git_releasableBranchRegex" ] && RELEASABLE_BRANCHES=$git_releasableBranchRegex

# PROJECT_NAME='gorm-tools'
CHANGELOG_NAME="docs/release-notes.md"

# cats key files into a cache-checksum.tmp file for circle to use as key
# change this based on how project is structured
function catKeyFiles {
  cat gradle.properties build.gradle plugin/build.gradle examples/restify/build.gradle examples/app-domains/build.gradle > cache-checksum.tmp
}

function compile {
  # Downloads Dependencies
  ./gradlew resolveConfigurations
  ./gradlew classes
  ./gradlew testClasses
  ./gradlew integrationTestClasses
}

function check {
  ./gradlew check --max-workers=2
}

# helper/debug function ex: `build.sh logVars test sqlserver`
function logVars {
  # initEnv ${1:-dev} ${2:-mysql}
  for varName in $BUILD_VARS; do
    echo "$varName = ${!varName}"
  done
}

# --- boiler plate function runner, keep at end of file ------
# BASH_SOURCE check will be true if this is run, false if imported into another script with `source`
if [[ "${#BASH_SOURCE[@]}" == 1 ]]; then
  source build/bin/function_runner.sh
fi

