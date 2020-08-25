#!/usr/bin/env bash
# --------------------------------------------
# Variables and helper functions for the build and makefile
# done in bash so it can be more flexible.
# To show the BUILD_VARS run `./build.sh logVars <env> <dbms>`
# substituting <evn> with test,dev, seed and <dbms> with sqlserver or mysql
# --------------------------------------------
# if build/bin does not exists then clone the bin scripts
[ ! -e build/bin ] && git clone https://github.com/yakworks/bin.git build/bin --single-branch --depth 1;
# user.env overrides
[[ -f user.env ]] && source user.env
# import build_functions.sh which has the setVar, dbEnvInit, createEnvFile, etc functions
source build/bin/build_functions.sh
# create variables from yaml
source build/bin/yaml
# parse_yaml gradle/build.yml && echo
create_yml_variables gradle/build.yml
echo "github_fullName ${github_fullName}"
# source the version
source version.properties
# call setVersion
setVersion $version
[ "$snapshot" == "true" ] && VERSNAP="-SNAPSHOT"
#should match whats in settings.gradle
PROJECT_NAME='gorm-tools'

source build/bin/github_pages
source build/bin/docmark

# helper/debug function ex: `build.sh logVars test sqlserver`
function logVars {
  initEnv ${1:-dev} ${2:-mysql}
  for varName in $BUILD_VARS; do
      echo "$varName = ${!varName}"
  done
}

# --- boiler plate function runner, keep at end of file ------
# BASH_SOURCE check will be true if this is run, false if imported into another script with `source`
if [[ "${#BASH_SOURCE[@]}" == 1 ]]; then
  source build/bin/function_runner.sh
fi

