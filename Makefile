# check for build/shipkit and clone if not there, this should come first
SHIPKIT_DIR = build/shipkit
$(shell [ ! -e $(SHIPKIT_DIR) ] && git clone -b v1.0.46 https://github.com/yakworks/shipkit.git $(SHIPKIT_DIR) >/dev/null 2>&1)
# Shipkit.make first, which does all the lifting to create makefile.env for the BUILD_VARS
include $(SHIPKIT_DIR)/Shipkit.make
include $(SHIPKIT_DIR)/makefiles/vault.make
include $(SHIPKIT_DIR)/makefiles/spring-common.make
include $(SHIPKIT_DIR)/makefiles/ship-gh-pages.make
# DB = true # set this to true to turn on the DB environment options

## Run spotlessApply and normal check
check:
	$(gradlew) spotlessApply check

# should run vault.decrypt before this,
# sets up github, kubernetes and docker login
ship.authorize: git.config-bot-user kubectl.config dockerhub.login
	$(logr.done)

## publish the java jar lib to repo.9ci for snapshot and to both for prod Sonatype Maven Central
publish:
	if [ "$(dry_run)" ]; then
		echo "🌮 dry_run ->  $(gradlew) publish"
	else
		if [ "$(IS_SNAPSHOT)" ]; then
			$(logr) "publishing SNAPSHOT"
			$(gradlew) publishJavaLibraryPublicationToMavenRepository
		else
			$(logr) "publishing to repo.9ci"
			$(gradlew) publishJavaLibraryPublicationToMavenRepository
			$(logr) "publishing to Sonatype Maven Central"
			$(gradlew) publishToSonatype closeAndReleaseSonatypeStagingRepository
		fi
		$(logr.done) "published"
	fi


ifdef RELEASABLE_BRANCH_OR_DRY_RUN

# removed  ship.docker kube.deploy for now
 ship.release: build publish
	$(logr.done)

 ship.docker: docker.app-build docker.app-push
	$(logr.done) "docker built and pushed"

 kube.deploy: kube.create-ns kube.clean
	$(kube_tools) apply_tpl $(APP_KUBE_SRC)/app-configmap.tpl.yml
	$(kube_tools) apply_tpl $(APP_KUBE_SRC)/app-deploy.tpl.yml
	$(logr.done)

else

 ship.release:
	$(logr.done) "not on a RELEASABLE_BRANCH, nothing to do"

endif # end RELEASABLE_BRANCH

# ---- Docmark -------

# the "dockmark-build" target depends on this. depend on the docmark-copy-readme to move readme to index
docmark.build-prep: docmark.copy-readme

# --- Testing and misc, here below is for testing and debugging ----

PORT ?= 8080
# sanity checks api with curl -i -G http://localhost:8081/api/rally/org,
# pass PORT=8081 for other than default 8080, only works when security is turned off
api-sanity-check:
	curl -i -G http://localhost:$(PORT)/api/rally/org/1

api-check-login:
	curl -H "Content-Type: application/json" -X POST -d '{"username":"admin","password":"Br1ck#ouse"}' \
		http://localhost:8080/api/login

## gets token from curl and used that for sanity check
api-check-with-token:
	curl_call="curl --silent -H 'Content-Type: application/json' -X POST \
		-d '{\"username\":\"admin\",\"password\":\"Br1ck#ouse\"}' \
		http://localhost:$(PORT)/api/login"
	resp=`eval "$$curl_call"`
	echo -e "login response: $$resp \n"
	token=`echo $$resp | awk -F'"' '/access_token/{ print $$(NF-1) }'`
	curl_call="curl -G -H 'Authorization: Bearer $$token' http://localhost:$(PORT)/api/rally/org/1"
	echo -e "$$curl_call \n"
	eval $$curl_call
	echo -e "\n$@ success"

## login to the deployed restify app, see notes for `api-check-login`
api-check-deployed-login:
	curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST \
	  -d '{"username":"admin","password":"123Foo"}' https://$(APP_KUBE_INGRESS_URL)/api/login

## use TOKEN after api-check-deployed-login ex: `make api-check-deployed-with-token TOKEN=asdfasdfasdf`
api-check-deployed-with-token:
	curl -i -G -H "Authorization: Bearer $(TOKEN)" https://$(APP_KUBE_INGRESS_URL)/api/rally/org/1

# -- helpers --

ifdef IS_SNAPSHOT
# publish snapsot to repo.9ci
 publish.snapshot.repo:
	./gradlew publishJavaLibraryPublicationToMavenRepository
endif

## shows gorm-tools:dependencies --configuration runtime
gradle.dependencies:
	# ./gradlew gorm-tools:dependencies --configuration compileClasspath runtimeClasspath
	./gradlew rally-api:dependencies --configuration compileClasspath

## runs the benchmark tests
run-benchmarks:
	$(gradlew) benchmarks:assemble
	cd examples/benchmarks
	java -server -Xmx3g -XX:MaxMetaspaceSize=256m \
		-DmultiplyData=3 -Dgorm.tools.async.poolSize=4 -Djava.awt.headless=true \
        -Dgrails.env=prod -jar build/libs/benchmarks.jar

#		-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
#		-XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly \
#		-XX:+CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=70 \
#		-XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark \
#		-XX:SurvivorRatio=8 \

## start the restify example jar
start.restify: # start.db
	${gradlew} restify:assemble
	cd examples/restify
	java -server -Xmx2g -jar build/libs/restify.jar

#oapi.docs-publish:
#	cp -r api-docs/build/_site/. build/api-docs
#	cp api-docs/build/api.yaml build/api-docs
#	$(MAKE) oapi.docs-push
#	$(logr.done)

# builds the eleventy docs with npm
oapi.docs-build:
	cd api-docs
	npm install
	npm run build
	$(logr.done)

# clones the api-docs branch or this project where we will publish/push
#oapi.docs-clone:
#	mkdir -p $(BUILD_DIR) && rm -rf "$(API_DOCS_BUILD_DIR)"
#	git clone $(GITHUB_URL) $(API_DOCS_BUILD_DIR) -b $(API_DOCS_BRANCH) --single-branch --depth 1
#	$(logr.done)

# pushes the changes to the api-docs branch, the way the docs works is it pulls from repo on docker startup and builds
oapi.docs-push:
	git -C $(API_DOCS_BUILD_DIR) add -A .
	# or true so doesnt blow error if no changes
	git -C $(API_DOCS_BUILD_DIR) commit -a -m "CI API docs published [skip ci]" || true
	git -C $(API_DOCS_BUILD_DIR) push -q $(GITHUB_URL) $(API_DOCS_BRANCH) || true
	$(logr.done)

## starts the eleventy server for the api docs, assumes the oapi.generate-api-yaml has been generated already into the dist
oapi.start:
	cd api-docs && npm run start

## Runs the test that generates the api.yml from the domains.
oapi.generate-api-yaml:
	# HACK, rm test-results to force a run if only editing yaml
	rm -rf examples/restify/build/test-results
	${gradlew} restify:integrationTest --tests *OpenapiGeneratorSpec*

## generates api yaml with grails test and runs oapi.start
oapi.generate-start: oapi.generate-api-yaml oapi.start

oapi.bundle:
	cd api-docs
	npm run oapi:bundle

oapi.build: oapi.generate-api-yaml oapi.bundle

## run to get into builder shell
# make oapi.generate-apy-yaml
# make oapi.shell
# cd api-docs
# npm install   # ONLY ONCE PER DOCKER! If you have a docker from last time, don't reinstall npm
# npm run start
## Now you're in a docker shell for running oapi
oapi.shell:
	docker run --name oapi-shell -it --rm \
	  -v `pwd`:/project:delegated  \
	  -p 4567:4567 \
	  yakworks/builder:node14 /bin/bash

