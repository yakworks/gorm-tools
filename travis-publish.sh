#!/usr/bin/env bash

set -e

if [[ -n $TRAVIS_TAG ]] || [[ $TRAVIS_BRANCH == 'master' && $TRAVIS_REPO_SLUG == "yakworks/gorm-tools" && $TRAVIS_PULL_REQUEST == 'false' ]]; then

    if [[ -n $TRAVIS_TAG ]]
    then
        echo "### publishing release to BinTray"
        ./gradlew gorm-tools:bintrayUpload --no-daemon
    else
         echo "### publishing snapshot"
        ./gradlew gorm-tools:publish --no-daemon
    fi

    if [[ $TRAVIS_BRANCH == 'master' ]]
    then

        echo "### publishing docs"
        git config --global user.name "9cibot"
        git config --global user.email "9cibot@9ci.com"
        git config --global credential.helper "store --file=~/.git-credentials"
        echo "https://$GITHUB_TOKEN:@github.com" > ~/.git-credentials
        git clone https://${GITHUB_TOKEN}@github.com/yakworks/gorm-tools.git -b gh-pages gh-pages --single-branch > /dev/null

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
    fi

else
  echo "Not a Tag or Not on master branch, not publishing"
  echo "TRAVIS_BRANCH: $TRAVIS_BRANCH"
  echo "TRAVIS_TAG: $TRAVIS_TAG"
  echo "TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
  echo "TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"
fi
