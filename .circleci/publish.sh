#!/usr/bin/env bash

set -e

# only build when its not a branch and not a pull request
if [[ $CIRCLE_BRANCH == 'master' &&  -z "$CIRCLE_PULL_REQUEST" ]]
then
  echo "condtions satisfied to publish"
  if [[ "$CIRCLE_TAG" =~ ^v[0-9].* ]]; then
      echo "### publishing release to BinTray"
      gradle gorm-tools:bintrayUpload --no-daemon
  else
       echo "### publishing SNAPSHOT"
      gradle gorm-tools:publish --no-daemon
  fi

  gradle gitPublishPush --no-daemon --max-workers 2
  
else
  echo "nothing to do here"
fi
