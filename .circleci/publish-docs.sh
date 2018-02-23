#!/usr/bin/env bash

set -e

curl "https://bootstrap.pypa.io/get-pip.py" -o "get-pip.py"
sudo python get-pip.py
pip install --user -r pip-requirements.txt

# only build when its not a branch and not a pull request
if [[ $CIRCLE_BRANCH == 'master'] && [ -z "$CIRCLE_PULL_REQUEST" ]]
then

  python3 -m mkdocs build

  cd gh-pages
  cp -r ../site/. . #Copy Mkdocs

  mkdir -p api #Copy Java API
  cp -r ../plugin/build/docs/groovydoc/. ./api

  git add .

  #If there are any changes, do commit and push
  if [[ -n $(git status -s) ]]
  then
      git commit -a -m "Update docs for Travis build: https://travis-ci.org/$TRAVIS_REPO_SLUG/builds/$TRAVIS_BUILD_ID"
      git push origin HEAD
  else
      echo "### No changes to docs"
  fi

  cd ..
  rm -rf gh-pages
  rm -rf site
  echo "### Done publishing docs"

else
  echo "Not a Tag or Not on master branch, not publishing"
  echo "TRAVIS_BRANCH: $TRAVIS_BRANCH"
  echo "TRAVIS_TAG: $TRAVIS_TAG"
  echo "TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
  echo "TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"
fi
