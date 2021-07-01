SHELL := /bin/bash
MAKEFLAGS += -rR
build.sh := ./build.sh
# DB = true
shResults := $(shell $(build.sh)) # call build.sh first without args which will git clone scripts to build/bin
include ./build/bin/Makefile-core.make # core includes
# include the helper makefiles for project
include $(BUILD_BIN)/make/gradle.make
include $(BUILD_BIN)/make/docker.make

# $(info shResults $(shResults)) # logs out the bash echo from shResults
# $(info DBMS=$(DBMS) BUILD_ENV=$(BUILD_ENV) DOCK_BUILDER_NAME=$(DOCK_BUILDER_NAME) DOCK_DB_BUILD_NAME=$(DOCK_DB_BUILD_NAME) DockerExec=$(DockerExec) DockerDbExec=$(DockerDbExec))

# publish the lib and release files
ci-publish-lib:
	./build.sh ci-publish-lib

# publish the lib and release files
ci-publish-docs:
	./build.sh ci-publish-docs

# --- helpers -----
## run the docs server locally
dockmark-serve:
	./build.sh dockmark-serve

## shows gorm-tools:dependencies --configuration compile
show-compile-dependencies:
	# ./gradlew gorm-tools:dependencies --configuration compileClasspath
	./gradlew gorm-tools:dependencies --configuration compile

