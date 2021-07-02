#!/usr/bin/env bash
# --------------------------------------------
# main bash build script for CI, dev and releasing. Used in Makefile
# --------------------------------------------
set -e  # Abort script at first error, when a command exits with non-zero status (except in until or while loops, if-tests, list constructs)
# set -u  # Attempt to use undefined variable outputs error message, and forces an exit
# set -x  # Similar to verbose mode (-v), but expands commands
# set -o pipefail  # Causes a pipeline to return the exit status of the last command in the pipe that returned a non-zero return value.

# if build/bin scripts do not exists then clone it
[ ! -e build/bin ] && git clone https://github.com/yakworks/bin.git build/bin  -b 2.1 # --single-branch --depth 1
# .env overrides for local dev, not to be checked in
[ -f .env ] && set -o allexport && source .env && set +o allexport
source build/bin/all.sh # consolidates most of the helpful scripts from bin
# ---- more imports/"source" files here, keep build.sh light. create a scripts dir and keem them there
# source scripts/build_support.sh

# --- build vars that need to be setup before initEnv. If gradle, then setup gradle/build.yml too -----
# list of projects used to spin through, build the checksum and consolidate the test reports for circle
setVar GRADLE_PROJECTS "gorm-tools gorm-tools-rest gorm-tools-security rally-domain examples/restify examples/testify"

# function is called after initEnv is run
# put variables here that depend on the initEnv
postInitEnv(){
  setVar FOO BAR
}

# initialize environment for dev by default, keep at end before function runs
initEnv dev

# --- boiler plate function runner, keep at end of file ------
if declare -f "$1" > /dev/null; then
  "$@" #call function with arguments verbatim
else
  [ "$1" ] && echo "'$1' is not a known function name" >&2 && exit 1
fi
