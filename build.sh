#!/usr/bin/env bash
# --------------------------------------------
# main bash build script for CI, dev and releasing. make calls this too.
# --------------------------------------------
set -e
# if build/bin does not exists then clone the bin scripts
[ ! -e build/bin ] && git clone https://github.com/yakworks/bin.git build/bin  -b 2.1 # --single-branch --depth 1
# user.env overrides for local dev, not to be checked in
[[ -f user.env ]] && source user.env
# all.sh consolidates most of the helpful scripts from bin
source build/bin/all.sh

# default init from yml file
init_from_build_yml "gradle/build.yml"
# echo "PROJECT_NAME $PROJECT_NAME"

# list of projects used to spin through, build the checksum and consolidate the test reports for circle
setVar GRADLE_PROJECTS "gorm-tools gorm-tools-rest gorm-tools-security rally-domain examples/restify examples/testify"

# --- boiler plate function runner, stay at end of file ------
if declare -f "$1" > /dev/null; then
  "$@" #call function with arguments verbatim
else
  [ "$1" ] && echo "'$1' is not a known function name" >&2 && exit 1
fi
