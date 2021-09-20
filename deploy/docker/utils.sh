#!/bin/bash

set -e

# ---
# trims leading and trailing spaces from string
# $1 - the string to trim
# ---
trim() {
    local trimmed="$1"

    # Strip leading spaces.
    while [[ $trimmed == ' '* ]]; do
       trimmed="${trimmed## }"
    done
    # Strip trailing spaces.
    while [[ $trimmed == *' ' ]]; do
        trimmed="${trimmed%% }"
    done

    echo "$trimmed"
}

# build app props from list which will be in form
#  dataSource.host=mysql
#  dataSource.dbName=bar
# turned into -DdataSource.host=mysql -DdataSource.dbName=bar
# $1 - the string to convert into java props
# ---
transform_to_java_props() {
  sysProps=""
  while IFS= read -r line; do
    trimLine=$(trim $line)
    # if value of $var starts with #, ignore it
    [[ $trimLine =~ ^#.* ]] && continue
    # if its empty then move on
    [[ -z "$trimLine" ]] && continue

    sysProps+="-D$trimLine "
  done <<< "$1"

  echo "$sysProps"
}
