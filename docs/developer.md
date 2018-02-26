# Introduction
This document describes the release process designed and implemented for `gorm-tools plugin`. Its main purpose is to explain to developers and maintainers how to prepare and release a new version of this plugin.

## The Process
We publish snapshots and releases for the plugin.
Snapshots are published from master branch. The artifacts are released to 9ci artifactory using ```gradle publish``` command.
 
Releases are published to BinTray from tags, every time a tag is pushed.

Release process is automated. 
Snapshots and releases are automatically published by travis build.

### Releasing a new snapshot
Snapshots gets published automatically from master branch. Just pushing any commits to master will trigger a travis build and new snapshot will get published.

### Publish a new release
New releases are published from tags. Follow the following steps to publish a new release.

- branch off from master
- increment version number (projectVersion) in gradle.properties file
- Make any other code changes if required.
- Push branch
- Create a tag, new releases will be automatically published to bintray by travis.
- If any code changes were made to the branch, merge back to master
- increment the snapshot version in master branch

We create a new branch for release tag release from the branch so that if we have to make any specific changes for release we can do it in branch.
And we do update the version in branch and not master. Also we can have different release branches for Grails 3.2.x vs Grails 3.3.x

## Project Structure

### Versioning
https://semver.org/

Best practices for setting up a plugin project

### Spotless
Spotless is a general-purpose formatting plugin that checks and fixes code and markdown

https://github.com/ajoberstar/gradle-defaults/blob/master/src/main/groovy/org/ajoberstar/gradle/defaults/DefaultsPlugin.groovy
https://github.com/diffplug/spotless/tree/master/plugin-gradle
https://github.com/diffplug/goomph/blob/v1.0.0/build.gradle#L78-L99

### Build num and releasing
https://jpragmainc.wordpress.com/2016/07/12/project-release-and-version-management-using-gradle-and-git/
http://devdeeds.com/auto-increment-build-number-using-gradle-in-android/
https://github.com/researchgate/gradle-release
https://github.com/riiid/gradle-github-plugin
