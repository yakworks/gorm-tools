#!/usr/bin/env bash

set -e

export CI_COMMIT_MESSAGE=`git log --format="%s" -n 1 $CIRCLE_SHA1`

commitRange=$(echo "$CIRCLE_COMPARE_URL" | rev | cut -d/ -f1 | rev)
echo $commitRange

if [[ $(git diff --name-only $commitRange | grep --invert-match -E "(README\.md|mkdocs\.yml|docs/)") ]]; then
  echo "Testing - has changes that are not docs"
  ./gradlew  clean
  ./gradlew  gorm-tools:check --no-daemon --max-workers 2
  ./gradlew  test-app:check --no-daemon --max-workers 2

  if [[ $CIRCLE_BRANCH == 'master' ]]; then
    ./gradlew build -s && ./gradlew ciPublishVersion
    # if grep -q 'snapshot=true' version.properties
    # then
    # if [[ "$CIRCLE_TAG" =~ ^v[0-9].* ]]; then
    #  echo "### publishing release to BinTray"
    #  ./gradlew  gorm-tools:bintrayUpload --no-daemon
    #else
    #  echo "### publishing SNAPSHOT"
    #  ./gradlew publishVersion
    #fi
  fi
fi

# DO Docs if branch its master, not a pull request and there are doc changes
if [[ $CIRCLE_BRANCH == 'master' ]]; then
  if [[ $(git diff --name-only $commitRange | grep -E "(README\.md|mkdocs\.yml|docs/)") ]]; then
    echo "has doc changes"
    git config --global user.name "9cibot"
    git config --global user.email "9cibot@9ci.com"
    git config --global credential.helper "store --file=~/.git-credentials"
    ./gradlew  gitPublishPush --no-daemon --max-workers 2
  fi
fi

