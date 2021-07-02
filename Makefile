SHELL := /bin/bash
MAKEFLAGS += -rR
build.sh := ./build.sh
# DB = true
shResults := $(shell $(build.sh)) # call build.sh first without args which will git clone scripts to build/bin
include ./build/bin/Makefile-core.make # core includes
# include the helper makefiles for project
include $(BUILD_BIN)/make/gradle.make
include $(BUILD_BIN)/make/docker.make
include $(BUILD_BIN)/make/docmark.make

.PHONY: publish-release publish-lib

## runs the full release publish
publish-release:

## publish the library jar, gradle publish if a gradle project
publish-lib:

# NOT_SNAPSHOT := $(if $(IS_SNAPSHOT),,true)
# ifneq (,$(and $(RELEASABLE_BRANCH),$(NOT_SNAPSHOT)))

ifdef RELEASABLE_BRANCH

 publish-lib:
	@if [ "$(IS_SNAPSHOT)" ]; then echo "publishing SNAPSHOT"; else echo "publishing release"; fi
	./gradlew publish

 publish-release: publish-lib
	@if [ ! "$(IS_SNAPSHOT)" ]; then \
		echo "not a snapshot ... doing a full release and version bump"; \
		$(build.sh) releaseFiles $(RELEASABLE_BRANCH); \
	fi;

 publish-docs:
	@if [ ! "$(IS_SNAPSHOT)" ]; then \
		echo "not a snapshot, publishing docs"; \
		$(MAKE) docmark-publish-prep; \
		$(MAKE) git-push-pages; \
	fi;

	@if [ "$(IS_SNAPSHOT)" ]; then echo "pushing SNAPSHOT docs"; else echo "pushing release docs"; fi
	@ # $(MAKE) docmark-publish-prep

endif # end RELEASABLE_BRANCH

NOT_SNAPSHOT := $(if $(IS_SNAPSHOT),,true)
# $(info NOT_SNAPSHOT $(NOT_SNAPSHOT))
# for now, only publish is its NOT as snapshot and its is releasable
ifneq (,$(and $(RELEASABLE_BRANCH),$(NOT_SNAPSHOT)))

docmark-git-push:
	@if [ "$(IS_SNAPSHOT)" ]; then echo "pushing SNAPSHOT docs"; else echo "pushing release docs"; fi
	@ # $(MAKE) git-push-pages

endif # end RELEASABLE_BRANCH

# $(info shResults $(shResults)) # logs out the bash echo from shResults

## shows gorm-tools:dependencies --configuration compile
show-compile-dependencies:
	# ./gradlew gorm-tools:dependencies --configuration compileClasspath
	./gradlew gorm-tools:dependencies --configuration compile

