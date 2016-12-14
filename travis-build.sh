#!/bin/bash

echo "### Running tests"

cd dao-app && ./gradlew test

echo "### Running publishing"

if [[ $TRAVIS_BRANCH == 'grails3' && $TRAVIS_PULL_REQUEST == 'false' ]]; then
	echo "### publishing plugin to bintray"
	cd ../dao-plugin && ./gradlew bintrayUpload

else
  echo "TRAVIS_BRANCH: $TRAVIS_BRANCH"
  echo "TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
  echo "TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"
fi