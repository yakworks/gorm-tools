#!/usr/bin/env bash
# --------------------------------------------
# main bash build script for CI, dev and releasing. Used in Makefile
# --------------------------------------------
set -eo pipefail # strict mode https://bit.ly/36MvF0T
source build/shipkit/bin/init_env
# source build/bin/init_docker_builders # main init script

# NOTE: keep build.sh light & simples. create a script with helper functions in a script dir and source it in
# source scripts/build_support.sh

# -- Build Var Environemnts that can't be in build.yml
# setVar VAULT_URL https://github.com/9ci/vault.git

# --- boiler plate function runner, keep at end of file ------
# check if first param is a functions
arg1="${1:-}"
if declare -f "$arg1" > /dev/null; then
  init_env # initialize standard environment, reads version.properties, build.yml , etc..
  "$@" #call function with arguments verbatim
else # could be that nothing passed or what was passed is invalid
  [ "$arg1" ] && echo "'$arg1' is not a known function name" >&2 && exit 1
fi
