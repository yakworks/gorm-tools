SHELL := /bin/bash
MAKEFLAGS += -rR
# -- bin and sh scripts variables --
BINDIR := ./build/bin
kube.sh := $(BINDIR)/kubernetes
circle := $(BINDIR)/circle
gw := ./gradlew

# call it first to git clone the build/bn
shResults := $(shell ./build.sh)
# include boilerplate to set BUILD_ENV and DB from targets
include $(BINDIR)/Makefile-env-db.make
# calls the build.sh makeEnvFile to build the vairables file for make, recreates each make run
shResults := $(shell ./build.sh makeEnvFile $(BUILD_ENV) $(DB_VENDOR) $(USE_BUILDER))
# import/sinclude the variables file to make it availiable to make as well
sinclude ./build/make/$(BUILD_ENV)_$(DB_VENDOR).env
# include common makefile templates
include $(BINDIR)/Makefile-docker.make
# include $(BINDIR)/Makefile-gradle.make
# include $(BINDIR)/Makefile-kube.make
include $(BINDIR)/Makefile-help.make

# $(info shResults $(shResults)) # logs out the bash echo from shResults
# $(info DBMS=$(DBMS) BUILD_ENV=$(BUILD_ENV) DOCK_BUILDER_NAME=$(DOCK_BUILDER_NAME) DOCK_DB_BUILD_NAME=$(DOCK_DB_BUILD_NAME) DockerExec=$(DockerExec) DockerDbExec=$(DockerDbExec))
# SELF_DIR := $(dir $(lastword $(MAKEFILE_LIST)))
# $(info SELF_DIR=$(SELF_DIR))

# --- Targets/Goals -----
# if not arguments ar provided then show help
.DEFAULT_GOAL := help

# --- Circle builds-----
cache-key-file: ## generates the cache-key.tmp for circle to checksum
	$(circle) cache-key-file "$(GRADLE_PROJECTS)"

## calls ./gradlew resolveConfigurations to download gradle deps
resolve-dependencies:
	./gradlew resolveConfigurations --no-daemon

check: ## call ./gradlew check to do a full lint and test
	./gradlew check

# merges test results in one spot to store in ci build
merge-test-results:
	$(circle) merge-test-results "$(GRADLE_PROJECTS)"

# publish the lib and release files
ci-publish-lib:
	./build.sh ci-publish-lib

# publish the lib and release files
ci-publish-docs:
	./build.sh ci-publish-docs

# --- helpers -----
dockmark-serve: ## run the docs server locally
	./build.sh dockmark-serve

show-compile-dependencies: ## shows gorm-tools:dependencies --configuration compile
	# ./gradlew gorm-tools:dependencies --configuration compileClasspath
	./gradlew gorm-tools:dependencies --configuration compile
