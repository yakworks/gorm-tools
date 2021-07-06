SHELL := /bin/bash
MAKEFLAGS += -rR
build.sh := ./build.sh
# DB = true
shResults := $(shell $(build.sh)) # call build.sh first without args which will git clone scripts to build/bin
include ./build/bin/Makefile-core.make # core includes
# include the helper makefiles for project
include $(BUILD_BIN)/make/circle.make
include $(BUILD_BIN)/make/gradle.make
include $(BUILD_BIN)/make/docker.make
include $(BUILD_BIN)/make/spring-docker.make
include $(BUILD_BIN)/make/docmark.make

.PHONY: publish-release publish-lib

# empty targets so make doesn't blow up when not a RELEASABLE_BRANCH
# runs the full release publish
publish-release:
# publish the library jar, gradle publish if a gradle project
publish-lib:
# publishes docs
publish-docs:

# NOT_SNAPSHOT := $(if $(IS_SNAPSHOT),,true)
# ifneq (,$(and $(RELEASABLE_BRANCH),$(NOT_SNAPSHOT)))

ifdef RELEASABLE_BRANCH

 publish-lib:
	@if [ "$(IS_SNAPSHOT)" ]; then echo "publishing SNAPSHOT"; else echo "publishing release"; fi
	./gradlew publish

 publish-release: publish-lib
	@if [ ! "$(IS_SNAPSHOT)" ]; then \
		echo "not a snapshot ... doing a full release and version bump"; \
		$(MAKE) release-it; \
	fi;

 publish-docs:
	@if [ ! "$(IS_SNAPSHOT)" ]; then \
		echo "not a snapshot, publishing docs"; \
		$(MAKE) docmark-publish; \
	else \
		echo "IS_SNAPSHOT ... NOT publishing docs "; \
	fi;

endif # end RELEASABLE_BRANCH

## gradle restify:bootRun
start:
	$(gw) restify:bootRun

## run the restify jar
start-jar:
	java -server -Xmx3048m -XX:MaxMetaspaceSize=256m \
    	-jar $(APP_JAR)

## starts the docker with the app jar, same docker that is deployed
start-docker-app: build/docker/built
	docker run --name=$(APP_NAME) -d \
    	--memory="3g" --memory-swap="3g" --memory-reservation="2g" \
    	--network builder-net \
    	-p 8081:8080 \
    	-e APP_PROPS="$(APP_PROPS)" \
    	$(APP_DOCKER_URL)

## stops the docker jar app
stop-docker-app:
	$(build.sh) docker_stop $(APP_NAME)

# -- helpers --
## shows gorm-tools:dependencies --configuration compile
show-compile-dependencies:
	# ./gradlew gorm-tools:dependencies --configuration compileClasspath
	./gradlew gorm-tools:dependencies --configuration compile

