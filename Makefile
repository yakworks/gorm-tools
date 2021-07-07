SHELL := /bin/bash
MAKEFLAGS += -rR
build.sh := ./build.sh
# DB = true
shResults := $(shell $(build.sh)) # call build.sh first without args which will git clone scripts to build/bin
include ./build/bin/Makefile-core.make # core includes
# include the helper makefiles for project
include $(BUILD_BIN)/makefiles/docker.make
include $(BUILD_BIN)/makefiles/jbuilder-docker.make
include $(BUILD_BIN)/makefiles/spring-docker.make
include $(BUILD_BIN)/makefiles/circle.make
include $(BUILD_BIN)/makefiles/docmark.make

.PHONY: publish-release
# runs the full release publish, empty targets so make doesn't blow up when not a RELEASABLE_BRANCH
publish-release:

ifdef RELEASABLE_BRANCH

 publish-release: publish-lib | _verify_RELEASABLE_BRANCH
	@if [ ! "$(IS_SNAPSHOT)" ]; then \
		echo "not a snapshot ... doing version bump, changelog and tag push"; \
		$(MAKE) release-tag; \
	fi;

endif # end RELEASABLE_BRANCH

# gradle restify:bootRun
# start:
# 	$(gw) restify:bootRun

PORT ?= 8080
## sanity checks api with curl -i -G http://localhost:8081/api/rally/org, pass PORT=8081 for other than default 8080
curl-sanity-check:
	curl -i -G http://localhost:$(PORT)/api/rally/org/1

# -- helpers --
## shows gorm-tools:dependencies --configuration compile
show-dependencies:
	# ./gradlew gorm-tools:dependencies --configuration compileClasspath
	./gradlew restify:dependencies --configuration runtime

run-benchmarks:
	@ $(gw) benchmarks:assemble
	@ cd examples/benchmarks; \
	java -server -Xmx3048m -XX:MaxMetaspaceSize=256m -jar \
	  -DmultiplyData=3 -Dgpars.poolsize=4 build/libs/benchmarks.war
	@ # -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap
