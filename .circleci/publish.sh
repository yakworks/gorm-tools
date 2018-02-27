#!/usr/bin/env bash

set -e

# only build when its not a branch and not a pull request
if [[ $CIRCLE_BRANCH == 'master'] && [ -z "$CIRCLE_PULL_REQUEST" ]]
then

  if [[ "$CIRCLE_TAG" ~= ^v\d.* ]]; then
      echo "### publishing release to BinTray"
      gradle gorm-tools:bintrayUpload --no-daemon
  else
       echo "### publishing snapshot"
      gradle gorm-tools:publish --no-daemon
  fi

else
  echo "nothing to do here"
fi
