#!/usr/bin/env bash
# --------------------------------------------
# main bash build script for CI, dev and releasing. Used in Makefile
# --------------------------------------------
set -e
# if build/bin scripts do not exists then clone it
[ ! -e build/bin ] && git clone https://github.com/yakworks/bin.git build/bin  -b 2.1 # --single-branch --depth 1
# .env overrides for local dev, not to be checked in
[ -f .env ] && set -o allexport && source .env && set +o allexport
source build/bin/all.sh # consolidates most of the helpful scripts from bin

# default init from yml file
init_from_build_yml "gradle/build.yml"
# echo "PROJECT_NAME $PROJECT_NAME"

# list of projects used to spin through, build the checksum and consolidate the test reports for circle
setVar GRADLE_PROJECTS "gorm-tools gorm-tools-rest gorm-tools-security rally-domain examples/restify examples/testify"

# --- boiler plate function runner, keep at end of file ------
if declare -f "$1" > /dev/null; then
  "$@" #call function with arguments verbatim
else
  [ "$1" ] && echo "'$1' is not a known function name" >&2 && exit 1
fi
