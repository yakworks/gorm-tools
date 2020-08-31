SHELL := /bin/bash
MAKEFLAGS += -rR
# -- bin and sh scripts --
build.sh := ./build.sh
DB_VENDOR := h2

# include boilerplate to set BUILD_ENV and DB from targets
include ./build/bin/Makefile-env-db.make
# calls the build.sh makeEnvFile to build the vairables file for make, recreates each make run
shResults := $(shell $(build.sh) makeEnvFile $(BUILD_ENV) $(DB_VENDOR) $(USE_BUILDER))
# import/sinclude the variables file to make it availiable to make as well
sinclude ./build/make/$(BUILD_ENV)_$(DB_VENDOR).env
# include common makefile templates
include ./build/bin/Makefile-docker.make
include ./build/bin/Makefile-gradle.make
include ./build/bin/Makefile-help.make

# $(info shResults $(shResults)) # logs out the bash echo from shResults
# $(info DBMS=$(DBMS) BUILD_ENV=$(BUILD_ENV) DOCK_BUILDER_NAME=${DOCK_BUILDER_NAME} DOCK_DB_BUILD_NAME=${DOCK_DB_BUILD_NAME} DockerExec=${DockerExec} DockerDbExec=${DockerDbExec})

# --- Targets/Goals -----
# if not arguments ar provided then show help
.DEFAULT_GOAL := help

build-log-vars: start-if-builder ## uses the build.sh to log vars
	${DockerExec} ${build.sh} logVars

dockmark-serve: ## run the docs server locally
	${build.sh} dockmark-serve
