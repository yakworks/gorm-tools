#!/usr/bin/env bash
# anything done in  body of script is done after version variables but before everyting else

# NOTE: any output from here, such as with 'echo', will be swallowed with make UNLESS you pass VERBOSE=true
# its best to use logit.info or logit.debug and it will kick out to the build/make/shikit.log

# setVar vs using = , registers is in the BUILD_VARS for make.
# this is normally fine as the variables in the init use setVar and will only assign values if it they are not set already
# setVar ACTIVE_BRANCH buzz

# setVar only sets it if it not already set. use putVar to override the variable
# putVar VERSION_POSTFIX -SNAP

# `make log-vars` should show this
setVar FOO bazz

# anything done inside post_init will run as the final step before make target to allow any custom variables and tasks
function post_init {
  # see note above, pass VERBOSE=true to make if you want to see this echo out
  echo "in post_init"
}
